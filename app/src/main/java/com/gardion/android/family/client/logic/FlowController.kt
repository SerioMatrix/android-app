package com.gardion.android.family.client.logic

import android.app.Activity
import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.gardion.android.family.client.R
import com.gardion.android.family.client.data.datasource.FlowData
import com.gardion.android.family.client.data.datasource.SharedPreferencesDataStore
import com.gardion.android.family.client.gardionui.*
import com.gardion.android.family.client.security.GardionConnectionService
import com.gardion.android.family.client.security.GardionDeviceAdminReceiver
import com.gardion.android.family.client.utils.GardionUtils
import java.security.KeyStore

class FlowController : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_ENABLE_ADMIN: Int = 200
        private const val REQUEST_EXPLAIN_ADMIN_SCREEN: Int = 101
        private const val REQUEST_GARDION_LOGIN: Int = 103
        private const val REQUEST_VPN_START: Int = 104
        private const val REQUEST_WELCOME_SCREEN: Int = 105
        private const val REQUEST_IMPORT_CERTIFICATE: Int = 107
        private const val REQUEST_SHOW_PARENT_PIN: Int = 108

        const val INTENT_PARENT_PIN: String = "intent_parent_pin"
    }

    private val TAG = FlowController::class.java.simpleName

    private lateinit var flowData: FlowData
    private lateinit var manager: DevicePolicyManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //is the right please to do this here?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            GardionUtils.createNotificationChannel(this,
                    getString(R.string.notification_channel_id_general),
                    getString(R.string.notification_channel_name_general),
                    getString(R.string.notification_channel_description_general))
        }

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
            !flowData.isVpnProfileSaved()!! -> startGardionLogin()
            !isDeviceAdminActive() -> startExplainAdminScreen()
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

    private fun startGardionConnectionService() {
        startService(Intent(this, GardionConnectionService::class.java))
    }

    private fun startShowParentPin(parentPin: String) {
        val intent = GardionShowParentPinActivity.getIntent(this)
        intent.putExtra(INTENT_PARENT_PIN, parentPin)
        startActivityForResult(intent, REQUEST_SHOW_PARENT_PIN)
    }

    private fun isDeviceAdminActive(): Boolean {
        return manager.isAdminActive(GardionDeviceAdminReceiver.getComponentName(this))
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
            REQUEST_GARDION_LOGIN -> handleGardionLogin(data, resultCode)
            REQUEST_EXPLAIN_ADMIN_SCREEN -> handleExplainAdminScreen()
            REQUEST_CODE_ENABLE_ADMIN -> handleDeviceAdminCreation()
            REQUEST_IMPORT_CERTIFICATE -> handleGardionCertificate(resultCode)
            REQUEST_VPN_START -> handleVpnStart()
            REQUEST_SHOW_PARENT_PIN -> handleShowParentPin()
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
        startGardionLogin()
    }

    private fun handleExplainAdminScreen() {
        askForDeviceAdmin()
    }

    private fun handleVpnStart() {
        finish()
    }

    private fun handleShowParentPin(){
        flowData.setGlobalPasswordCreated(true)
        startExplainAdminScreen()
    }

    private fun handleGardionLogin(data: Intent?, resultCode: Int) {
        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                flowData.userCertificateUsed(data.extras.getBoolean(GardionLoginActivity.INTENT_CERTIFICATE_USED))
                startShowParentPin(data.extras.getString(GardionLoginActivity.INTENT_PARENT_PIN))
            } else {
                flowData.userCertificateUsed(false)
                finish()
            }
        } else {
            finish()
        }
    }

    private fun startVpnService() {
        startActivityForResult(GardionVpnActivity.getIntent(this), REQUEST_VPN_START)
    }

    private fun handleDeviceAdminCreation() {
        isCertificateInstalled("hellO")
        if(flowData.isUserCertificateUsed()!!) {
            startCertificateInstallation()
        } else {
            startVpnService()
            startGardionConnectionService()
        }
    }

    private fun startGardionLogin() {
        startActivityForResult(GardionLoginActivity.getIntent(this), REQUEST_GARDION_LOGIN)
    }

    private fun askForDeviceAdmin() {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, GardionDeviceAdminReceiver.getComponentName(this))
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Please enable Gardion as Device Admin")
        startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN)
    }

    private fun isCertificateInstalled(userCertificateAlias: String) {
        val ks = KeyStore.getInstance("AndroidCAStore")

        //check null?
        val aliases = ks.aliases()
        while(aliases.hasMoreElements()) {
            val alias = aliases.nextElement()
            Log.d(TAG, "Alias: $alias")
        }
    }
}