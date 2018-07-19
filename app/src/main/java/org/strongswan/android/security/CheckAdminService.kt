package org.strongswan.android.security

import android.app.Service
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import org.strongswan.android.R
import org.strongswan.android.data.datasource.DataStore
import org.strongswan.android.data.datasource.SharedPreferencesDataStore
import org.strongswan.android.gardionui.GardionEnableAdminActivity
import org.strongswan.android.network.GardionApi
import org.strongswan.android.network.GardionServerEventManager
import org.strongswan.android.network.model.GardionEvent
import java.util.concurrent.TimeUnit

class CheckAdminService : Service() {

    private val binder: IBinder = EnableAdminLocalBinder()

    private lateinit var manager: DevicePolicyManager
    private lateinit var dataStore: DataStore

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        manager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

        val sharedPrefs = this.getSharedPreferences(SharedPreferencesDataStore.PREFERENCES_NAME, Context.MODE_PRIVATE)
        dataStore = SharedPreferencesDataStore(sharedPrefs)
        informServerAboutDeactivation()

        Observable.interval(3000L, TimeUnit.MILLISECONDS)
                .timeInterval()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { showEnableAdminScreen() }

        return START_STICKY
    }

    private fun informServerAboutDeactivation() {
        val eventManager = GardionServerEventManager(this)
        eventManager.sendGardionEvent(GardionServerEventManager.GardionEventType.ADMIN_DEACTIVATION)
    }

    private fun isDeviceAdminActive(): Boolean {
        return manager.isAdminActive(GardionDeviceAdminReceiver.getComponentName(this))
    }

    private fun showEnableAdminScreen() {
        if (isDeviceAdminActive()) {
            stopSelf()
        } else {
            val launchActivity = Intent(this, GardionEnableAdminActivity::class.java)
            startActivity(launchActivity)
        }
    }

    override fun onBind(bindIntent: Intent?): IBinder {
        return binder
    }

    class EnableAdminLocalBinder : Binder() {
        fun getService(): CheckAdminService {
            return CheckAdminService()
        }
    }

}