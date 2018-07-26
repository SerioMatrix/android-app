package com.gardion.android.family.client.logic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.gardion.android.family.client.data.datasource.SharedPreferencesDataStore
import com.gardion.android.family.client.gardionui.GardionVpnActivity
import org.strongswan.android.utils.GardionUtils

class UserPresentReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val sharedPrefs = context?.applicationContext?.getSharedPreferences(SharedPreferencesDataStore.PREFERENCES_NAME, Context.MODE_PRIVATE)
        val dataStore = SharedPreferencesDataStore(sharedPrefs!!)
        if (intent?.action == Intent.ACTION_USER_PRESENT && dataStore.isVpnProfileSaved()!!) {
            if (!GardionUtils.isVpnConnected(context)) {
                val gardionIntent = Intent(context, GardionVpnActivity::class.java)
                gardionIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                gardionIntent.putExtra(GardionVpnActivity.KEY_IS_FROM_USER_PRESENT_RECEIVER, true)
                context.applicationContext?.startActivity(gardionIntent)
            }
        }
    }
}