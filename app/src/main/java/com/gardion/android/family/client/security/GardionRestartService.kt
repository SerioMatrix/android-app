package com.gardion.android.family.client.security

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.util.Log
import com.gardion.android.family.client.data.datasource.SharedPreferencesDataStore
import com.gardion.android.family.client.gardionui.GardionVpnActivity
import com.gardion.android.family.client.logic.GardionRestartReceiver
import java.util.*
import java.lang.Exception

class GardionRestartService: Service() {

    private val binder: IBinder = ConnectionLocalBinder()

    companion object {
        var handler: Handler = Handler()
        lateinit var runnable: Runnable
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("GARDION_CONNECTION", "${javaClass.simpleName} started")
        //val handler = Handler()
        runnable = object : Runnable {
            override fun run() {
                restartApp()
                handler.postDelayed(this, 5000)
            }
        }
        handler.post(runnable)
        return START_STICKY
    }

    private fun restartApp() {
        val sharedPrefs = this.getSharedPreferences(SharedPreferencesDataStore.PREFERENCES_NAME, Context.MODE_PRIVATE)
        val flowData = SharedPreferencesDataStore(sharedPrefs)
        Log.d("GARDION_CONNECTION", "${javaClass.simpleName} restartApp?")
        Log.d("GARDION_CONNECTION", "isVpnActivityOpen: ${flowData.isVpnActivityOpen()}")
        Log.d("GARDION_CONNECTION", "${javaClass.simpleName}: disabledIllegally: ${flowData.isGardionDisabledIllegal()}")

        if(!flowData.isVpnActivityOpen()!!) {
            val intent = Intent(this, GardionVpnActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val sharedPrefs = this.getSharedPreferences(SharedPreferencesDataStore.PREFERENCES_NAME, Context.MODE_PRIVATE)
        val flowData = SharedPreferencesDataStore(sharedPrefs)

        Log.d("GARDION_CONNECTION", "${javaClass.simpleName} onDestroy")
        Log.d("GARDION_CONNECTION", "${javaClass.simpleName}: disabledIllegally: ${flowData.isGardionDisabledIllegal()}")

        if(flowData.isGardionDisabledIllegal()!!){
            val broadcastIntent = Intent(applicationContext, GardionRestartReceiver::class.java)
            sendBroadcast(broadcastIntent)
            Log.d("GARDION_CONNECTION", "sendBroadcast to GardionRestartReceiver")
        } else {
            stopSelf()
            try {
                handler.removeCallbacks(runnable)
            } catch (e: Exception) {
                Log.e("GARDION_CONNECTION", e.toString())
            }
        }
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    class ConnectionLocalBinder : Binder() {
        fun getService(): GardionRestartService {
            return GardionRestartService()
        }
    }
}
