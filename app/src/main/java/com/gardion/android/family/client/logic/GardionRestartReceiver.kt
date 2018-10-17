package com.gardion.android.family.client.logic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.gardion.android.family.client.data.datasource.SharedPreferencesDataStore
import android.util.Log
import com.gardion.android.family.client.gardionui.GardionVpnActivity

class GardionRestartReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("GARDION_CONNECTION", "receiver ${javaClass.simpleName}")

        val sharedPrefs = context.getSharedPreferences(SharedPreferencesDataStore.PREFERENCES_NAME, Context.MODE_PRIVATE)
        val flowData = SharedPreferencesDataStore(sharedPrefs)
        if(flowData.isGardionDisabledIllegal()!!) {
            Log.d("GARDION_CONNECTION", "RestartReceiver is starting RestartService")
            ReceiverUtils.connectVpnOnReceive(context, GardionVpnActivity.KEY_IS_FROM_DEACTIVATED_VPN)
        }
    }
}