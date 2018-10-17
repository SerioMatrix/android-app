package com.gardion.android.family.client.utils

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.gardion.android.family.client.data.datasource.SharedPreferencesDataStore


class GardionUtils {

    companion object {

        //TODO - check if this is really the easiest way to implement this functionality
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

        fun makeRequest(activity: Activity) {
            ActivityCompat.requestPermissions(activity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    100)
        }

        fun isVpnReady(context: Context?): Boolean {
            val sharedPrefs = context?.applicationContext?.getSharedPreferences(SharedPreferencesDataStore.PREFERENCES_NAME, Context.MODE_PRIVATE)
            val dataStore = SharedPreferencesDataStore(sharedPrefs!!)
            return dataStore.isVpnProfileSaved()!! &&
                    (!dataStore.isUserCertificateUsed()!! ||
                            (dataStore.isUserCertificateUsed()!! && dataStore.isUserCertificateChosen()!!))
        }

        //TODO - getRunningServices is deprecated check
        fun isMyServiceRunning(serviceClass: Class<*>, context: Context): Boolean {
            val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.name == service.service.className) {
                    return true
                }
            }
            return false
        }


       //TODO - check preferences, parameterize?
        //TODO - check if we leave this here
        @RequiresApi(Build.VERSION_CODES.O)
        fun createNotificationChannel(context: Context, channelId: String, name: String, descriptionText: String,
                                      importance: Int = NotificationManager.IMPORTANCE_DEFAULT) {
            val mChannel = NotificationChannel(channelId, name, importance)
            mChannel.description = descriptionText
            val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }



    }
}