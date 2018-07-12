package org.strongswan.android.logic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.strongswan.android.gardionui.GardionVpnActivity
import org.strongswan.android.utils.GardionUtils

class UserPresentReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        if (intent?.action == Intent.ACTION_USER_PRESENT) {
            if (!GardionUtils.isVpnConnected(context)) {
                val gardionIntent = Intent(context, GardionVpnActivity::class.java)
                gardionIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                gardionIntent.putExtra(GardionVpnActivity.KEY_IS_FROM_USER_PRESENT_RECEIVER, true)
                context?.applicationContext?.startActivity(gardionIntent)
            }
        }
    }
}