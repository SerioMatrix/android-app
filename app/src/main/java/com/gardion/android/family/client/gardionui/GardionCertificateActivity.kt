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
import com.gardion.android.family.client.data.datasource.SharedPreferencesDataStore
import kotlinx.android.synthetic.main.activity_gardion_certificate.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import org.strongswan.android.data.VpnProfileDataSource

class GardionCertificateActivity : AppCompatActivity() {

    private var job: Job = Job()
    private lateinit var dataStore: DataStore
    private val INSTALL_PKCS12 = 100
    private val TAG =  this::class.java.name

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

        Log.d(TAG, pkcs12Base64.toString())

        if(pkcs12Base64 != null) {
            job.cancel()
            job = launch(CommonPool) {
                importPkcs12(pkcs12Base64, alias)
            }
        } else {
            Log.e(TAG, "installCertificate: no PKCS12 found in dataStore")
        }
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
            Log.d(TAG, e.toString())
        }
    }


    private fun chooseKey(defaultAlias: String) {
        val keyChainAliasCallback = KeyChainAliasCallback {
            if(it != null) {
                Log.d(TAG, "installed Certificate: " + it.toString())
                finishActivity()
            }
            else {
                Log.d(TAG, "not installed")
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
            //intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivityForResult(intent, INSTALL_PKCS12)
        } catch (e: Exception) {
            Log.d(TAG, e.toString())
        }
    }

    private fun finishActivity() {
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == INSTALL_PKCS12) {
            if(resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "installed certificate")
                chooseKeyForVpn()
            }
            if(resultCode == Activity.RESULT_CANCELED) {
                Log.d(TAG, "failed installing credentials")
            }
        }
    }
}
