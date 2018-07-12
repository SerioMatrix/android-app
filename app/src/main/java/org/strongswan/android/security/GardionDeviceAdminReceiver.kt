package org.strongswan.android.security

import android.app.admin.DeviceAdminReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import org.strongswan.android.gardionui.EnableProfileActivity
import org.strongswan.android.toast

class GardionDeviceAdminReceiver: DeviceAdminReceiver() {

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
    }

    override fun onDisableRequested(context: Context?, intent: Intent?): CharSequence {
        return super.onDisableRequested(context, intent)
    }

    override fun onDisabled(context: Context?, intent: Intent?) {
        context?.toast("Device Admin disabled")
    }

    override fun onPasswordChanged(context: Context?, intent: Intent?) {
        context?.toast("Password Changed")
    }
}