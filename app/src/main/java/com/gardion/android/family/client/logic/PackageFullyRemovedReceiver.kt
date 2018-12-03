package com.gardion.android.family.client.logic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.gardion.android.family.client.gardionui.GardionVpnActivity

class PackageFullyRemovedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("GARDION_RECEIVER", "${this::class.java.simpleName}: broadcast ACTION_PACKAGE_FULLY_REMOVED")
        if (intent?.action == Intent.ACTION_PACKAGE_FULLY_REMOVED) {
            Log.d("GARDION_RECEIVER", intent.data.toString())
        }
    }
}

