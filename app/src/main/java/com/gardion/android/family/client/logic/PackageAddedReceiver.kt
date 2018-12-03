package com.gardion.android.family.client.logic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.gardion.android.family.client.gardionui.GardionVpnActivity

class PackageAddedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("GARDION_RECEIVER", "${this::class.java.simpleName}: broadcast ACTION_PACKAGE_ADDED")
        if (intent?.action == Intent.ACTION_PACKAGE_ADDED) {
            //ReceiverUtils.connectVpnOnReceive(context, GardionVpnActivity.KEY_IS_FROM_USER_PRESENT_RECEIVER)
            Log.d("GARDION_RECEIVER", intent.data.toString())
        }
    }
}

