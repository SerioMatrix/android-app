package org.strongswan.android.logic

import android.app.Activity
import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import org.strongswan.android.data.datasource.FlowData
import org.strongswan.android.data.datasource.SharedPreferencesDataStore
import org.strongswan.android.gardionui.GardionEnableProfileActivity
import org.strongswan.android.gardionui.GardionLoginActivity
import org.strongswan.android.gardionui.GardionVpnActivity
import org.strongswan.android.gardionui.GardionPasswordCreatorActivity
import org.strongswan.android.security.CheckAdminService
import org.strongswan.android.security.GardionConnectionService
import org.strongswan.android.security.GardionDeviceAdminReceiver

class FlowController : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_ENABLE_ADMIN: Int = 200

        private const val REQUEST_PASSWORD_CREATION: Int = 100
        private const val REQUEST_DEVICE_ADMIN_INFO_SCREEN: Int = 101
        private const val REQUEST_PROFILE_OWNER: Int = 102
        private const val REQUEST_GARDION_LOGIN: Int = 103
        private const val REQUEST_VPN_START: Int = 104
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
            !flowData.isDeviceAdminFirstSet()!! && !isDeviceAdminActive() -> startEnableAdminService()
            !flowData.isGlobalPasswordCreated()!! -> startPasswordCreationScreen()
            !flowData.isVpnProfileSaved()!! -> showGardionLoginScreen()
            else -> startVpnService()
        }
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
            REQUEST_PASSWORD_CREATION -> handlePasswordCreation(data, resultCode)
            REQUEST_CODE_ENABLE_ADMIN -> handleDeviceAdminCreation()
            REQUEST_PROFILE_OWNER -> handleProfileOwnerCreation(data, resultCode)
            REQUEST_GARDION_LOGIN -> handleGardionLogin(resultCode)
            REQUEST_VPN_START -> handleVpnStart()
        }
    }

    private fun handleVpnStart() {
        finish()
    }

    private fun handleGardionLogin(resultCode: Int) {
        if (resultCode == Activity.RESULT_OK) {
            startVpnService()
            startGardionConnectionService()
        } else {
            finish()
        }
    }

    private fun startVpnService() {
        startActivityForResult(GardionVpnActivity.getIntent(this), REQUEST_VPN_START)
    }

    private fun handleProfileOwnerCreation(data: Intent?, resultCode: Int) {

    }

    private fun handleDeviceAdminCreation() {
        if (isDeviceAdminActive()) {
            showGardionLoginScreen()
        } else {
            finish()
        }
    }

    private fun askForProfileOwner() {
        startActivityForResult(GardionEnableProfileActivity.getIntent(this), REQUEST_PROFILE_OWNER)
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
                askForDeviceAdmin()
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