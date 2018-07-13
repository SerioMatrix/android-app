package org.strongswan.android.logic

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import org.strongswan.android.data.datasource.DataStore
import org.strongswan.android.data.datasource.SharedPreferencesDataStore
import org.strongswan.android.gardionui.PasswordCreatorActivity
import org.strongswan.android.security.GardionDeviceAdminReceiver
import org.strongswan.android.utils.KeyStoreManager

class FlowController : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_ENABLE_ADMIN: Int = 200

        private const val REQUEST_PASSWORD_CREATION: Int = 100
        private const val REQUEST_DEVICE_ADMIN: Int = 101
        private const val REQUEST_PROFILE_OWNER: Int = 102
        private const val REQUEST_GARDION_LOGIN: Int = 103
    }

    private lateinit var dataStore: DataStore

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        val sharedPrefs = this.getPreferences(Context.MODE_PRIVATE)
        dataStore = SharedPreferencesDataStore(sharedPrefs)
        startPasswordCreationScreen()
    }

    private fun startPasswordCreationScreen() {
        startActivityForResult(PasswordCreatorActivity.getIntent(this), REQUEST_PASSWORD_CREATION)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_PASSWORD_CREATION -> handlePasswordCreation(data, resultCode)
            REQUEST_CODE_ENABLE_ADMIN -> handleDeviceAdminCreation()
            REQUEST_PROFILE_OWNER -> handleProfileOwnerCreation(data, resultCode)
            REQUEST_GARDION_LOGIN -> handleGardionLogin(data, resultCode)
        }
    }

    private fun handleGardionLogin(data: Intent?, resultCode: Int) {

    }

    private fun handleProfileOwnerCreation(data: Intent?, resultCode: Int) {

    }

    private fun handleDeviceAdminCreation() {
        val manager: DevicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        if (manager.isAdminActive(GardionDeviceAdminReceiver.getComponentName(this))){
            showGardionLoginScreen()
        }
    }

    private fun showGardionLoginScreen() {

    }

    private fun handlePasswordCreation(data: Intent?, resultCode: Int) {
        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                // Initialize KeyStore and generate key
                KeyStoreManager.generateKey()
                val password = data.getStringExtra(PasswordCreatorActivity.INTENT_EXTRA_PASSWORD)
                //password encryption
                val encryptedPassword: String = KeyStoreManager.encryptData(password)
                //save encrypted password to sharedPreferences
                dataStore.saveEncryptedPass(encryptedPassword)
                dataStore.setGlobalPasswordSet(true)
                askForDeviceAdmin()
            } else {
                dataStore.setGlobalPasswordSet(false)
            }
        } else {
            dataStore.setGlobalPasswordSet(false)
        }
    }

    private fun askForDeviceAdmin() {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, GardionDeviceAdminReceiver.getComponentName(this))
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "We just want to test Gardion as Device Admin")
        startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN)
    }
}