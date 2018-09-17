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
        val sharedPrefs = context?.applicationContext?.getSharedPreferences(SharedPreferencesDataStore.PREFERENCES_NAME, Context.MODE_PRIVATE)
        val dataStore = SharedPreferencesDataStore(sharedPrefs!!)
        Log.d("GARDION_CONNECTION", "BOOT_COMPLETED")
        if ("android.intent.action.BOOT_COMPLETED" == intent?.action && dataStore.isVpnProfileSaved()!!) {
            if (GardionUtils.isNetworkAvailable(context)!!) {
                val gardionIntent = Intent(context, GardionVpnActivity::class.java)
                gardionIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                gardionIntent.putExtra(GardionVpnActivity.KEY_IS_FROM_BOOT_RECEIVER, true)
                context.applicationContext?.startActivity(gardionIntent)
            } else {
                GardionNetwork.requestNetworkInternet(context)
            }


        }
    }
}