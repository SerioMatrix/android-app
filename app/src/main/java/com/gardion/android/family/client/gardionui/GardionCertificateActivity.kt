package com.gardion.android.family.client.gardionui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.security.KeyChain
import android.security.KeyChainAliasCallback
import android.util.Base64
import android.util.Log
import com.gardion.android.family.client.R
import com.gardion.android.family.client.data.datasource.DataStore
import com.gardion.android.family.client.data.datasource.FlowData
import com.gardion.android.family.client.data.datasource.SharedPreferencesDataStore
import com.gardion.android.family.client.logic.FlowController
import kotlinx.android.synthetic.main.activity_gardion_certificate.*
import kotlinx.android.synthetic.main.activity_password_creator.*
import org.strongswan.android.data.VpnProfileDataSource

class GardionCertificateActivity : AppCompatActivity() {

    private lateinit var flowData: FlowData

    //TODO - check if only dataStore is enough here and we dont need exte flowData
    private lateinit var dataStore: DataStore
    private val INSTALL_PKCS12 = 100
    companion object {


        fun getIntent(activity: Activity): Intent {
            return Intent(activity, GardionCertificateActivity::class.java)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gardion_certificate)

        start_initial_setup_button.setOnClickListener {installCertificate(this)}
    }

    private fun installCertificate(context: Context?) {
        val sharedPrefs = context?.applicationContext?.getSharedPreferences(SharedPreferencesDataStore.PREFERENCES_NAME, Context.MODE_PRIVATE)
        val dataStore = SharedPreferencesDataStore(sharedPrefs!!)
        val pkcs12Base64 = dataStore.getConfigurationPkcs12()
        val alias = dataStore.getConfigurationUserCertificateAlias() ?: "Gardion User"

        Log.d("GARDION_CONNECTION", pkcs12Base64.toString())

        if(pkcs12Base64 != null) {
            importPkcs12(pkcs12Base64, alias)
        } else {
            Log.e("GARDION_CERTIFICATE", "installCertificate: no PKCS12 found in dataStore")
        }
        //chooseKeyForVpn()
    }

    private fun chooseKeyForVpn() {
        try {
            val database = VpnProfileDataSource(this)
            database.open()
            val vpnProfile = database.allVpnProfiles[0]
            database.close()
            if (vpnProfile.vpnType.toString() == "IKEV2_EAP_TLS") {
                chooseKey(vpnProfile.userCertificateAlias)
            }
        } catch (e: Exception) {
            Log.d("GARDION_CERTIFICATE", e.toString())
        }
    }


    private fun chooseKey(defaultAlias: String) {
        val keyChainAliasCallback = KeyChainAliasCallback {
            if(it != null) {
                Log.d("GARDION_CERTIFICATE", "installed Certificate: " + it.toString())
                finishActivity()
            }
            else {
                Log.d("GARDION_CERTIFICATE", "not installed")
            }
        }
        KeyChain.choosePrivateKeyAlias(this, keyChainAliasCallback, null, null, null, -1, defaultAlias)
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

    private fun finishActivity() {
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //TODO - check if / how to make onActivityResult for chooseKey work
        //atm always returns RESULT_CANCELED might be a known bug (https://issuetracker.google.com/issues/37030890)
        //TODO - move to activity gardion certificate
        if(requestCode == INSTALL_PKCS12) {
            if(resultCode == Activity.RESULT_OK) {
                Log.d("GARDION_CREDENTIALS", "installed credentials")
            }
            if(resultCode == Activity.RESULT_CANCELED) {
                Log.d("GARDION_CREDENTIALS", "failed installing credentials")
            }
        }
    }
}
