package com.gardion.android.family.client.gardionui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_gardion_login.*
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import com.gardion.android.family.client.R
import com.gardion.android.family.client.data.VpnProfile
import com.gardion.android.family.client.data.VpnProfileDataSource
import com.gardion.android.family.client.data.VpnType
import com.gardion.android.family.client.data.datasource.DataStore
import com.gardion.android.family.client.data.datasource.SharedPreferencesDataStore
import com.gardion.android.family.client.network.GardionApi
import com.gardion.android.family.client.network.GardionLinks
import com.gardion.android.family.client.network.model.GardionData
import com.gardion.android.family.client.toast
import com.gardion.android.family.client.utils.GardionUtils
import org.apache.http.conn.ConnectTimeoutException
import java.net.SocketTimeoutException
import java.util.*


class GardionLoginActivity : AppCompatActivity() {

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
        login_button.setOnClickListener { buttonLogin() }
        contact_support_button.setOnClickListener { GardionLinks(this).goToForum() }
    }

    private fun buttonLogin() {
        val gardionCode = gardion_login_pincode.text.toString()

        when {
            !isGardionCodeValid(gardionCode) -> toast(getString(R.string.login_toast_too_short))
            !GardionUtils.isNetworkAvailable(this)!! -> toast(getString(R.string.general_toast_device_offline))
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

    private fun finishWithData(result: Int) {
        val data = Intent()
        setResult(result, data)
        finish()
    }

    private fun saveToDataBase(gardionData: GardionData) {
        val gardionUrl = gardionData.connection.url[0]
        val gardionUsername = gardionData.connection.authentication.name
        val gardionPassword = gardionData.connection.authentication.password
        val vpnProfile = VpnProfile()
        vpnProfile.name = "gardionVpn"
        vpnProfile.gateway = gardionUrl
        vpnProfile.username = gardionUsername
        vpnProfile.password = gardionPassword
        vpnProfile.uuid = UUID.randomUUID()
        vpnProfile.vpnType = VpnType.IKEV2_EAP
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

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
