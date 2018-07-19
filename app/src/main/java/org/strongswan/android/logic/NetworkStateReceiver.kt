package org.strongswan.android.logic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Bundle
import android.util.Log
import org.strongswan.android.data.VpnProfile
import org.strongswan.android.data.VpnProfileDataSource
import org.strongswan.android.gardionui.GardionVpnActivity

class NetworkStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val TAG = "NetworkStateReceiver"

        val connectivityManager: ConnectivityManager =
                context?.applicationContext?.getSystemService(Context.CONNECTIVITY_SERVICE)
                        as ConnectivityManager
//        val networkInfo: NetworkInfo = connectivityManager.activeNetworkInfo
        if (isVpnConnected(connectivityManager)){
            Log.d(TAG, "VPN is connected")
        } else {
            Log.d(TAG, "VPN is not connected")
        }
    }

    private fun isVpnConnected(connectivityManager: ConnectivityManager): Boolean {
        val networks: Array<out Network>? = connectivityManager.allNetworks
        for (i in networks?.indices!!) {
            val networkCapabilities: NetworkCapabilities = connectivityManager.getNetworkCapabilities(networks[i])
            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                return true
            }
        }
        return false
    }
}