package org.strongswan.android.security

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.strongswan.android.data.datasource.DataStore
import org.strongswan.android.data.datasource.SharedPreferencesDataStore
import org.strongswan.android.gardionui.GardionVpnActivity

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val sharedPrefs = context?.applicationContext?.getSharedPreferences(SharedPreferencesDataStore.PREFERENCES_NAME, Context.MODE_PRIVATE)
        val dataStore = SharedPreferencesDataStore(sharedPrefs!!)
        if ("android.intent.action.BOOT_COMPLETED" == intent?.action && dataStore.isVpnProfileSaved()!!) {
            val gardionIntent = Intent(context, GardionVpnActivity::class.java)
            gardionIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            gardionIntent.putExtra(GardionVpnActivity.KEY_IS_FROM_BOOT_RECEIVER, true)
            context.applicationContext?.startActivity(gardionIntent)
        }
    }
}