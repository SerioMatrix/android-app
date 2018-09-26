package com.gardion.android.family.client.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import org.strongswan.android.logic.VpnStateService

class GardionNetworkReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("GARDION_CONNECTION", "GardionNetworkReceiver")

        var mService: VpnStateService? = null
        val state: VpnStateService.State? = mService?.state

        Log.d("GARDION_CONNECTION", "${this::class.java.simpleName}: state: ${state.toString()}")

        when (state) {
            VpnStateService.State.CONNECTED, VpnStateService.State.CONNECTING -> Log.d("GARDION_CONNECTION", "connec")
            VpnStateService.State.DISCONNECTING, VpnStateService.State.DISABLED -> Log.d("GARDION_CONNECTION", "disconnec")
        }
    }
}