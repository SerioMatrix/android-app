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

class NetworkStateReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val TAG = "NetworkStateReceiver"
        Log.d(TAG, "Receiver starts")
        val profileInfo: Bundle = getProfileInfoBundle(getVpnDataFromDatabase(context?.applicationContext))
        Log.d(TAG, "Profile info fetched from DB$profileInfo")
        val connectivityManager: ConnectivityManager =
                context?.applicationContext?.getSystemService(Context.CONNECTIVITY_SERVICE)
                        as ConnectivityManager
        val networkInfo: NetworkInfo = connectivityManager.activeNetworkInfo
        if (networkInfo.isConnected && !isVpnConnected(connectivityManager)){
            val launchVpn = Intent(context, CharonVpnService::class.java)
            launchVpn.putExtras(profileInfo)
            Log.d(TAG, "RetrofitService starts")
            context.applicationContext?.startService(launchVpn)
        }
    }

    private fun isVpnConnected(connectivityManager: ConnectivityManager): Boolean {
        val networks: Array<out Network>? = connectivityManager.allNetworks
            for (i in networks?.indices!!) {
                val networkCapabilities: NetworkCapabilities = connectivityManager.getNetworkCapabilities(networks[i])
                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)){
                    return true
                }
        }
        return false
    }

    private fun getVpnDataFromDatabase(context: Context?): VpnProfile {
        val dataSource = VpnProfileDataSource(context)
        dataSource.open()
        val profile: VpnProfile = dataSource.getVpnProfile(1)
        dataSource.close()
        return profile

    }
    private fun getProfileInfoBundle(vpnData: VpnProfile): Bundle {
        val bundle = Bundle()
        bundle.putLong(VpnProfileDataSource.KEY_ID, vpnData.id)
        bundle.putString(VpnProfileDataSource.KEY_USERNAME, vpnData.username)
        bundle.putString(VpnProfileDataSource.KEY_PASSWORD, vpnData.password)
        bundle.putBoolean(GardionVpnActivity.PROFILE_REQUIRES_PASSWORD, true)
        bundle.putString(GardionVpnActivity.PROFILE_NAME, "gardion_test")
        return bundle
    }
}