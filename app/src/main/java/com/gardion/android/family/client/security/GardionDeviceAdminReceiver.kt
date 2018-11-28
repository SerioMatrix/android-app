package com.gardion.android.family.client.security

import android.app.admin.DeviceAdminReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.gardion.android.family.client.R
import com.gardion.android.family.client.data.datasource.DataStore
import com.gardion.android.family.client.data.datasource.SharedPreferencesDataStore
import com.gardion.android.family.client.network.GardionServerEventManager
import com.gardion.android.family.client.toast


class GardionDeviceAdminReceiver: DeviceAdminReceiver() {

    private var TAG = this::class.java.name
    private lateinit var dataStore: DataStore

    companion object {
        fun getComponentName(context: Context): ComponentName {
            return ComponentName(context.applicationContext, GardionDeviceAdminReceiver::class.java)
        }
    }

    override fun onEnabled(context: Context?, intent: Intent?) {
        val eventManager = GardionServerEventManager(context!!)
        eventManager.sendGardionEvent(GardionServerEventManager.GardionEventType.ADMIN_ACTIVATION)
        context?.stopService(Intent(context.applicationContext, CheckAdminService::class.java))
        context?.toast(context.getString(R.string.device_admin_toast_active))
        Log.d(TAG, "Device admin enabled")
    }

    override fun onDisabled(context: Context?, intent: Intent?) {
        val eventManager = GardionServerEventManager(context!!)
        eventManager.sendGardionEvent(GardionServerEventManager.GardionEventType.ADMIN_DEACTIVATION)
        val sharedPrefs = context?.getSharedPreferences(SharedPreferencesDataStore.PREFERENCES_NAME, Context.MODE_PRIVATE)
        dataStore = SharedPreferencesDataStore(sharedPrefs!!)
        dataStore.deviceAdminFirstSet(false)
        context.startService(Intent(context.applicationContext, CheckAdminService::class.java))
        context?.toast(context.getString(R.string.device_admin_toast_deactivated))
        Log.d(TAG, "Device admin disabled")
    }

    override fun onDisableRequested(context: Context?, intent: Intent?): CharSequence {
        return context?.getString(R.string.device_admin_warning_deactivation).toString()
    }

    override fun onPasswordChanged(context: Context?, intent: Intent?) {
        context?.toast(context.getString(R.string.device_admin_pw_changed))
    }
}