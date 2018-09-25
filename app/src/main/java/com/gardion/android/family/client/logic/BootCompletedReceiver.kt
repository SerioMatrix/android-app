package com.gardion.android.family.client.logic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.gardion.android.family.client.data.datasource.SharedPreferencesDataStore
import com.gardion.android.family.client.gardionui.GardionVpnActivity
import com.gardion.android.family.client.network.GardionNetwork
import com.gardion.android.family.client.utils.GardionUtils

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("GARDION_CONNECTION", "BOOT_COMPLETED")
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            ReceiverUtils.connectVpnOnReceive(context, GardionVpnActivity.KEY_IS_FROM_BOOT_RECEIVER)
        }
    }
}