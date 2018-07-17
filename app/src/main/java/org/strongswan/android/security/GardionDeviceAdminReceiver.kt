package org.strongswan.android.security

import android.app.admin.DeviceAdminReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import org.strongswan.android.data.datasource.DataStore
import org.strongswan.android.data.datasource.SharedPreferencesDataStore
import org.strongswan.android.gardionui.EnableProfileActivity
import org.strongswan.android.toast

class GardionDeviceAdminReceiver: DeviceAdminReceiver() {

    private lateinit var dataStore: DataStore

    companion object {
        fun getComponentName(context: Context): ComponentName{
            return ComponentName(context.applicationContext, GardionDeviceAdminReceiver::class.java)
        }
    }

    override fun onProfileProvisioningComplete(context: Context?, intent: Intent?) {
        val launch = Intent(context, EnableProfileActivity::class.java)
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context?.startActivity(launch)
    }

    override fun onEnabled(context: Context?, intent: Intent?) {
        context?.toast("Device Admin Active")
        context?.stopService(Intent(context.applicationContext, CheckAdminService::class.java))
    }

    override fun onDisabled(context: Context?, intent: Intent?) {
        context?.toast("Device Admin disabled")
        val sharedPrefs = context?.getSharedPreferences(SharedPreferencesDataStore.PREFERENCES_NAME, Context.MODE_PRIVATE)
        dataStore = SharedPreferencesDataStore(sharedPrefs!!)
        dataStore.deviceAdminFirstSet(false)
        context.startService(Intent(context.applicationContext, CheckAdminService::class.java))
    }

    override fun onPasswordChanged(context: Context?, intent: Intent?) {
        context?.toast("Password Changed")
    }
}