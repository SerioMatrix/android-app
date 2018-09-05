package com.gardion.android.family.client.utils

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.view.View
import android.view.inputmethod.InputMethodManager

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

        //TODO - check if Context? is necessary
        fun isInternetConnectionActive(context: Context): Boolean {
            val connectivityManager: ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networks: Array<out Network>? = connectivityManager.allNetworks
            for (i in networks?.indices!!) {
                val networkInfo: NetworkInfo = connectivityManager.getNetworkInfo(networks[i])
                if (networkInfo.isConnected) {
                    return true
                }
            }
            return false
        }

        fun isNetworkAvailable(context: Context): Boolean? {
            val connectivityManager: ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return connectivityManager.activeNetworkInfo?.isAvailable ?: false
        }


        fun forceKeyboardOpen(act: Activity){
            val imm: InputMethodManager = act.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        }

        fun hideKeyboard(activity: Activity) {
            val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            //Find the currently focused view, so we can grab the correct window token from it.
            var view = activity.currentFocus
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = View(activity)
            }
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}