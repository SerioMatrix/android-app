package com.gardion.android.family.client.logic

import android.app.Activity
import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.gardion.android.family.client.data.datasource.FlowData
import com.gardion.android.family.client.data.datasource.SharedPreferencesDataStore
import com.gardion.android.family.client.gardionui.*
import com.gardion.android.family.client.security.CheckAdminService
import com.gardion.android.family.client.security.GardionConnectionService
import com.gardion.android.family.client.security.GardionDeviceAdminReceiver

class FlowController : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_ENABLE_ADMIN: Int = 200

        private const val REQUEST_PASSWORD_CREATION: Int = 100
        private const val REQUEST_EXPLAIN_ADMIN_SCREEN: Int = 101
        private const val REQUEST_GARDION_LOGIN: Int = 103
        private const val REQUEST_VPN_START: Int = 104
        private const val REQUEST_WELCOME_SCREEN: Int = 105
        private const val REQUEST_IMPORT_CERTIFICATE: Int = 107
    }

    private val TAG = FlowController::class.java.simpleName

    private lateinit var flowData: FlowData
    private lateinit var manager: DevicePolicyManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        manager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val sharedPrefs = this.getSharedPreferences(SharedPreferencesDataStore.PREFERENCES_NAME, Context.MODE_PRIVATE)
        flowData = SharedPreferencesDataStore(sharedPrefs)
        if (!isMyServiceRunning(GardionConnectionService::class.java)) {
            try {
                startGardionConnectionService()
            } catch (e: Exception) {
                Log.w(TAG, "Unable to start service")
            }
        }
        when {
            flowData.isGardionFirstStart()!! -> startWelcomeScreen()
            !flowData.isGlobalPasswordCreated()!! -> startPasswordCreationScreen()
            //!flowData.isDeviceAdminFirstSet()!! || !isDeviceAdminActive() -> startExplainAdminScreen()
            !isDeviceAdminActive() -> startExplainAdminScreen()
            !flowData.isVpnProfileSaved()!! -> showGardionLoginScreen()
            flowData.isUserCertificateUsed()!! && !flowData.isUserCertificateChosen()!! -> startCertificateInstallation()
            else -> startVpnService()
        }
    }

    private fun startWelcomeScreen() {
        startActivityForResult(GardionWelcomeActivity.getIntent(this), REQUEST_WELCOME_SCREEN)
    }

    private fun startExplainAdminScreen() {
        startActivityForResult(GardionAdminActivity.getIntent(this), REQUEST_EXPLAIN_ADMIN_SCREEN)
    }

    private fun startEnableAdminService() {
        startService(Intent(this, CheckAdminService::class.java))
    }

    private fun startGardionConnectionService() {
        startService(Intent(this, GardionConnectionService::class.java))
    }

    private fun isDeviceAdminActive(): Boolean {
        return manager.isAdminActive(GardionDeviceAdminReceiver.getComponentName(this))
    }

    private fun startPasswordCreationScreen() {
        startActivityForResult(GardionPasswordCreatorActivity.getIntent(this), REQUEST_PASSWORD_CREATION)
    }

    private fun startCertificateInstallation() {
        startActivityForResult(GardionCertificateActivity.getIntent(this), REQUEST_IMPORT_CERTIFICATE)
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_WELCOME_SCREEN -> handleWelcomeScreen()
            REQUEST_PASSWORD_CREATION -> handlePasswordCreation(data, resultCode)
            REQUEST_EXPLAIN_ADMIN_SCREEN -> handleExplainAdminScreen()
            REQUEST_CODE_ENABLE_ADMIN -> handleDeviceAdminCreation()
            REQUEST_GARDION_LOGIN -> handleGardionLogin(data, resultCode)
            REQUEST_IMPORT_CERTIFICATE -> handleGardionCertificate(resultCode)
            REQUEST_VPN_START -> handleVpnStart()
        }
    }

    private fun handleGardionCertificate(resultCode: Int) {
        if (resultCode == Activity.RESULT_OK) {
            flowData.userCertificateChosen(true)
            startVpnService()
            startGardionConnectionService()
        } else {
            flowData.userCertificateChosen(false)
            finish()
        }
    }

    private fun handleWelcomeScreen() {
        flowData.gardionFirstStart(false)
        startPasswordCreationScreen()
    }

    private fun handleExplainAdminScreen() {
        askForDeviceAdmin()
    }

    private fun handleVpnStart() {
        finish()
    }

    private fun handleGardionLogin(data: Intent?, resultCode: Int) {
        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                flowData.userCertificateUsed(data.extras.getBoolean(GardionLoginActivity.INTENT_CERTIFICATE_USED))
            } else {
                flowData.userCertificateUsed(false)
            }

            if(flowData.isUserCertificateUsed()!!) {
                startCertificateInstallation()
            } else {
                startVpnService()
                startGardionConnectionService()
            }
        } else {
            finish()
        }
    }

    private fun startVpnService() {
        startActivityForResult(GardionVpnActivity.getIntent(this), REQUEST_VPN_START)
    }

    //TODO - check if that still make sense
    private fun handleDeviceAdminCreation() {
        if (isDeviceAdminActive()) {
            if(!flowData.isVpnProfileSaved()!!) {
                showGardionLoginScreen()
            } else {
                finish()
            }
        } else {
            finish()
        }
    }

    private fun showGardionLoginScreen() {
        startActivityForResult(GardionLoginActivity.getIntent(this), REQUEST_GARDION_LOGIN)
    }

    private fun handlePasswordCreation(data: Intent?, resultCode: Int) {
        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                // Initialize KeyStore and generate key
//                KeyStoreManager.generateKey()
//                val masterKey = KeyStoreManager.getAndroidKeyStoreKeyPair()
//                val password = data.getStringExtra(GardionPasswordCreatorActivity.INTENT_EXTRA_PASSWORD)
//                //password encryption
//                val encryptedPassword: String = KeyStoreManager.encryptData(password, masterKey?.public!!)
                //save encrypted password to sharedPreferences
                flowData.saveEncryptedPass(data.getStringExtra(GardionPasswordCreatorActivity.INTENT_EXTRA_PASSWORD))
                flowData.setGlobalPasswordCreated(true)
                startExplainAdminScreen()
            } else {
                flowData.setGlobalPasswordCreated(false)
            }
        } else {
            flowData.setGlobalPasswordCreated(false)
            finish()
        }
    }

    private fun askForDeviceAdmin() {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, GardionDeviceAdminReceiver.getComponentName(this))
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Please enable Gardion as Device Admin")
        startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN)
    }
}