package com.gardion.android.family.client.gardionui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.security.KeyChain
import android.security.KeyChainAliasCallback
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import android.util.Log
import kotlinx.android.synthetic.main.activity_gardion_login.*
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import com.gardion.android.family.client.R
import org.strongswan.android.data.VpnProfile
import org.strongswan.android.data.VpnProfileDataSource
import org.strongswan.android.data.VpnType
import com.gardion.android.family.client.data.datasource.DataStore
import com.gardion.android.family.client.data.datasource.SharedPreferencesDataStore
import com.gardion.android.family.client.network.GardionApi
import com.gardion.android.family.client.network.GardionLinks
import com.gardion.android.family.client.network.model.GardionData
import com.gardion.android.family.client.toast
import com.gardion.android.family.client.utils.GardionUtils
import com.google.gson.Gson
import java.io.File
import java.util.*


class GardionLoginActivity : AppCompatActivity() {

    private val INSTALL_PKCS12 = 100

    companion object {
        private var TAG = GardionLoginActivity::class.java.name

        fun getIntent(activity: Activity): Intent {
            return Intent(activity, GardionLoginActivity::class.java)
        }
    }

    private lateinit var dataStore: DataStore
    private val api: GardionApi = GardionApi.instance
    private var job: Job = Job()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gardion_login)
        val sharedPrefs = this.getSharedPreferences(SharedPreferencesDataStore.PREFERENCES_NAME, Context.MODE_PRIVATE)
        dataStore = SharedPreferencesDataStore(sharedPrefs)

        //TODO - remove devLocal = true only needed for dev
        GardionUtils.makeRequest(this)
        login_button.setOnClickListener { buttonLogin(true) }
        contact_support_button.setOnClickListener { GardionLinks(this).goToForum() }
    }

    private fun buttonLogin(devLocal: Boolean = false) {
        val gardionCode = gardion_login_pincode.text.toString()

        when {
            !isGardionCodeValid(gardionCode) -> toast(getString(R.string.login_toast_too_short))
            !GardionUtils.isNetworkAvailable(this)!! -> toast(getString(R.string.general_toast_device_offline))
            //TODO - remove case if not needed anymore for dev
            devLocal -> fetchDataLocal(gardionCode)
            else -> fetchData(gardionCode)
        }
    }

    private fun isGardionCodeValid(gardionCode: String): Boolean {
        return (gardionCode.length == 6)
    }

    private fun fetchData(gardionCode: String) {
        job.cancel()
        job = launch(CommonPool) {
            try {
                val response = api.fetchGardionData(gardionCode).execute()
                if (response.isSuccessful) {
                    val gardionProfile = response.body()!!

                    gardionProfile?.let { saveToDataBase(gardionProfile) }
                    withContext(UI, CoroutineStart.DEFAULT, {
                        toast(getString(R.string.login_toast_success))
                        finishWithData(Activity.RESULT_OK)
                    })
                } else {
                    val responseCode = response.code()
                    val errorMessage = when (responseCode) {
                        // if gardionCode does not exist at backend response code 500 is received
                        // code 500 can of course also result from different events on server
                        // server should actually send a message / code stating explicitly that the gardionCode does not exist
                        500 -> getString(R.string.login_toast_error_500)
                        else -> getString(R.string.login_toast_error_general)
                    }
                    withContext(UI, CoroutineStart.DEFAULT, {
                        toast("$errorMessage (Code: $responseCode)")
                    })
                }
            } catch (e: Exception) {
                Log.w(TAG, e.message)
                withContext(UI, CoroutineStart.DEFAULT, {
                    toast(getString(R.string.login_toast_error_general))
                })
            }
        }
    }

    //TODO - remove only for dev
    fun fetchDataLocal(gardionCode:String) {
        try {
            val input = File("sdcard/$gardionCode.json").readText()
            val gardionProfile  = Gson().fromJson(input, GardionData::class.java)
            Log.d("GARDION_PROFILE", gardionProfile.toString())
            gardionProfile?.let { saveToDataBase(gardionProfile) }
            finishWithData(Activity.RESULT_OK)
        } catch(e: Exception) {
            toast(e.toString())
        }
    }

    private fun finishWithData(result: Int) {
        val data = Intent()
        setResult(result, data)
        finish()
    }

    private fun saveToDataBase(gardionData: GardionData) {
        val gardionNameVpn = gardionData.connection.name
        val gardionUrl = gardionData.connection.url[0]
        val gardionUsername = gardionData.connection.authentication.name
        val gardionPassword = gardionData.connection.authentication.password
        val gardionVpnType = gardionData.connection.authentication.authType
        val gardionUserCertificateAlias = gardionData.connection.authentication.userCertificateAlias
        val pkcs12Base64 = gardionData.connection.authentication.pkcs12Base64

        //TODO save name of UserCertificate and use later when choose key?

        val vpnProfile = VpnProfile()
        vpnProfile.name = gardionNameVpn
        vpnProfile.gateway = gardionUrl
        vpnProfile.username = gardionUsername
        vpnProfile.password = gardionPassword
        vpnProfile.uuid = UUID.randomUUID()
        vpnProfile.vpnType = VpnType.valueOf(gardionVpnType)
        vpnProfile.userCertificateAlias = gardionUserCertificateAlias

        //TODO - extraActivity for this?
        if(gardionVpnType == "IKEV2_EAP_TLS") {
            importPkcs12(pkcs12Base64, gardionUserCertificateAlias)
            chooseKey(gardionUserCertificateAlias)
        }

        val database = VpnProfileDataSource(this)
        database.open()
        database.insertProfile(vpnProfile)
        database.close()
        dataStore.setVpnProfileSaved(true)
        saveConfigurationDataToSharedPrefs(gardionData)
    }

    private fun saveConfigurationDataToSharedPrefs(data: GardionData) {
        dataStore.saveConfigurationDeviceId(data.device.id)
        dataStore.saveConfigurationDeviceName(data.device.name)
    }

    private fun importPkcs12(pkcs12Base64: String, alias: String) {
        try {
            val pkcs12ByteArray = Base64.decode(pkcs12Base64, Base64.DEFAULT)
            val intent = KeyChain.createInstallIntent()
            intent.putExtra(KeyChain.EXTRA_NAME, alias)
            intent.putExtra(KeyChain.EXTRA_PKCS12, pkcs12ByteArray)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivityForResult(intent, INSTALL_PKCS12)
        } catch (e: Exception) {
            Log.d("GARDION_IMPORT_PKCS12", e.toString())
        }
    }

    private fun chooseKey(defaultAlias: String) {
        //TODO - change to meaningful Callback, can maybe be used for checking if user has successfully installed certificates
        val keyChainAliasCallback = KeyChainAliasCallback {toast("hallo")}
        //TODO - change null to better defaults
        KeyChain.choosePrivateKeyAlias(this, keyChainAliasCallback, null, null, null, -1, defaultAlias)
    }

    //TODO - check if / how to make onActivityResult work
    //atm always returns RESULT_CANCELED might be a known bug (https://issuetracker.google.com/issues/37030890)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == INSTALL_PKCS12) {
            if(resultCode == Activity.RESULT_OK) {
                Log.d("GARDION_CREDENTIALS", "installed credentials")
            }
            if(resultCode == Activity.RESULT_CANCELED) {
                Log.d("GARDION_CREDENTIALS", "failed installing credentials")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
