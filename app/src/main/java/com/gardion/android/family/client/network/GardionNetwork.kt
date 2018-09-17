package com.gardion.android.family.client.network

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_CANCEL_CURRENT
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import com.gardion.android.family.client.gardionui.GardionVpnActivity

class GardionNetwork {
    companion object {
        fun getBroadcastOnNetwork(context: Context?) {

            val manager = context?.applicationContext?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val intent = Intent(context, GardionNetworkReceiver::class.java)
            val request = NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build()
            val pendingIntent = PendingIntent.getBroadcast(context, 10, intent, FLAG_CANCEL_CURRENT)
            //<TODO - Pending Intent only added in AP level 23, find solution for lower API if function is needed
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                manager.registerNetworkCallback(request, pendingIntent)
            }
        }

        fun getActivityOnNetwork(context: Context?){
            val manager = context?.applicationContext?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val intent = Intent(context, GardionVpnActivity::class.java)

            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra(GardionVpnActivity.KEY_IS_FROM_NETWORK_AVAILABLE, true)


            val pendingIntent = PendingIntent.getActivity(context, 10, intent, FLAG_CANCEL_CURRENT)
            val request = NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                manager.registerNetworkCallback(request, pendingIntent)
            }
        }


        //TODO - best way to achieve the desired behavior? alt: registerNetworkCallback
        fun requestNetworkInternet(context: Context) {
            val manager = context?.applicationContext?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val request = NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build()
            val callback : ConnectivityManager.NetworkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network?) {
                    super.onAvailable(network)
                    Log.d("GARDION_CONNECTION", "onAvailable")
                    onAvailableNetwork(context)
                    manager.unregisterNetworkCallback(this)
                }
            }
            manager.requestNetwork(request, callback)
        }

        private fun onAvailableNetwork(context: Context) {
            Log.d("GARDION_CONNECTION", "startVPN")
            val gardionIntent = Intent(context, GardionVpnActivity::class.java)
            gardionIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            gardionIntent.putExtra(GardionVpnActivity.KEY_IS_FROM_NETWORK_AVAILABLE, true)
            context.applicationContext?.startActivity(gardionIntent)
        }
    }
}