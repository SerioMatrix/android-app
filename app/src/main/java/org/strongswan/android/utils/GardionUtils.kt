package org.strongswan.android.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo

class GardionUtils {

    companion object {
        fun isVpnConnected(context: Context?): Boolean {
            val connectivityManager: ConnectivityManager =
                    context?.applicationContext?.getSystemService(Context.CONNECTIVITY_SERVICE)
                            as ConnectivityManager
            val networks: Array<out Network>? = connectivityManager.allNetworks
            for (i in networks?.indices!!) {
                val networkCapabilities: NetworkCapabilities = connectivityManager.getNetworkCapabilities(networks[i])
                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                    return true
                }
            }
            return false
        }

        fun isInternetConnectionActive(context: Context?): Boolean {
            val connectivityManager: ConnectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networks: Array<out Network>? = connectivityManager.allNetworks
            for (i in networks?.indices!!) {
                val networkInfo: NetworkInfo = connectivityManager.getNetworkInfo(networks[i])
                if (networkInfo.isConnected) {
                    return true
                }
            }
            return false
        }
    }
}