package com.gardion.android.family.client.logic

import android.content.Context
import android.content.Intent
import com.gardion.android.family.client.gardionui.GardionVpnActivity
import com.gardion.android.family.client.network.GardionNetwork
import com.gardion.android.family.client.utils.GardionUtils

class ReceiverUtils {

    companion object {
        fun connectVpnOnReceive(context: Context?, keyStartedFrom: String) {
            if (!GardionUtils.isVpnConnected(context)) {
                if (GardionUtils.isVpnReady(context)) {
                    if (GardionUtils.isNetworkAvailable(context!!)!!) {
                        val gardionIntent = Intent(context, GardionVpnActivity::class.java)
                        gardionIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        gardionIntent.putExtra(keyStartedFrom, true)
                        context.applicationContext?.startActivity(gardionIntent)
                    } else {
                        GardionNetwork.requestNetworkInternet(context)
                    }
                }
            }
        }
    }

    
}