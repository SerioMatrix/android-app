package com.gardion.android.family.client.gardionui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.support.v7.app.AppCompatActivity
import android.view.View.GONE
import android.view.View.VISIBLE
import kotlinx.android.synthetic.main.activity_popup_gardion.*
import com.gardion.android.family.client.R
import com.gardion.android.family.client.data.datasource.DataStore
import com.gardion.android.family.client.data.datasource.FlowData
import com.gardion.android.family.client.data.datasource.SharedPreferencesDataStore
import com.gardion.android.family.client.security.CheckAdminService
import com.gardion.android.family.client.toast
import com.gardion.android.family.client.utils.GardionUtils

class GardionPopupActivity : AppCompatActivity() {

    private lateinit var flowData: FlowData
    private lateinit var dataStore: DataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_popup_gardion)
        enable_admin_go_to_settings_button.setOnClickListener { goToDeviceAdminSettings() }
        unlock_gardion_button.setOnClickListener { unlockGardion() }
        unlock_view_group.visibility = VISIBLE
    }

    override fun onStart() {
        super.onStart()
        unlock_view_group.visibility = VISIBLE
        secured_content_group.visibility = GONE
    }

    override fun onStop() {
        super.onStop()
        popup_ask_password_editText.text.clear()
    }

    private fun unlockGardion() {
        val sharedPrefs = this.getSharedPreferences(SharedPreferencesDataStore.PREFERENCES_NAME, Context.MODE_PRIVATE)
        dataStore = SharedPreferencesDataStore(sharedPrefs)
        val savedParentPin =  dataStore.getConfigurationParentPin()
        when (savedParentPin) {
            GardionUtils.hashSha256(popup_ask_password_editText.text.toString()) -> unlockTillLockScreen()
            else -> toast(getString(R.string.password_toast_pin_wrong))
        }
    }

    private fun unlockTillLockScreen() {
        stopService(Intent(this, CheckAdminService::class.java))
        val sharedPrefs = this.getSharedPreferences(SharedPreferencesDataStore.PREFERENCES_NAME, Context.MODE_PRIVATE)
        flowData = SharedPreferencesDataStore(sharedPrefs)
        flowData.setGardionUnlocked(true)
        toast(getString(R.string.popup_toast_unlocked))
        showSecuredContent()
    }

    private fun showSecuredContent() {
        unlock_view_group.visibility = GONE
        secured_content_group.visibility = VISIBLE
        secure_uninstall_gardion_button.setOnClickListener { uninstallGardion() }
    }

    private fun uninstallGardion() {
        val uninstallIntent = Intent(Intent.ACTION_DELETE)
        uninstallIntent.data = Uri.parse("package:$packageName")
        finish()
        startActivity(uninstallIntent)
    }

    private fun goToDeviceAdminSettings() {
        startActivity(Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.DeviceAdminSettings")))
    }
}
