package org.strongswan.android.security

import android.app.Service
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.strongswan.android.gardionui.GardionEnableAdminActivity
import java.util.concurrent.TimeUnit

class CheckAdminService : Service() {

    private val binder: IBinder = EnableAdminLocalBinder()

    private lateinit var manager: DevicePolicyManager


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        manager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

        Observable.interval(3000L, TimeUnit.MILLISECONDS)
                .timeInterval()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { showEnableAdminScreen() }

        return START_STICKY
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