package org.strongswan.android.security

import android.app.admin.DeviceAdminReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import org.strongswan.android.gardionui.EnableAdminActivity
import org.strongswan.android.gardionui.EnableProfileActivity
import org.strongswan.android.logic.FlowController
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
        val launchFlowController = Intent(context, FlowController::class.java)
        context?.startActivity(launchFlowController)
    }

    override fun onDisabled(context: Context?, intent: Intent?) {
        context?.toast("Device Admin disabled")
        val launchActivity = Intent(context, EnableAdminActivity::class.java)
        launchActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context?.startActivity(launchActivity)
    }

    override fun onPasswordChanged(context: Context?, intent: Intent?) {
        context?.toast("Password Changed")
    }
}