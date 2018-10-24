package com.gardion.android.family.client.logic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.gardion.android.family.client.gardionui.GardionVpnActivity

class UserPresentReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("GARDION_RECEIVER", "${this::class.java.simpleName}: broadcast ACTION_USER_PRESENT")
        if (intent?.action == Intent.ACTION_USER_PRESENT) {
            ReceiverUtils.connectVpnOnReceive(context, GardionVpnActivity.KEY_IS_FROM_USER_PRESENT_RECEIVER)
        }
    }
}

