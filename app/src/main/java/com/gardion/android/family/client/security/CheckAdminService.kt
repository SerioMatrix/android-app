package com.gardion.android.family.client.security

import android.app.PendingIntent
import android.app.Service
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Binder
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import com.gardion.android.family.client.R
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import com.gardion.android.family.client.data.datasource.DataStore
import com.gardion.android.family.client.data.datasource.SharedPreferencesDataStore
import com.gardion.android.family.client.gardionui.GardionPopupActivity
import com.gardion.android.family.client.network.GardionServerEventManager
import java.util.concurrent.TimeUnit

class CheckAdminService : Service() {

    private val binder: IBinder = EnableAdminLocalBinder()

    private lateinit var manager: DevicePolicyManager
    private lateinit var dataStore: DataStore
    private lateinit var disposable: Disposable

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        manager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

        val sharedPrefs = this.getSharedPreferences(SharedPreferencesDataStore.PREFERENCES_NAME, Context.MODE_PRIVATE)
        dataStore = SharedPreferencesDataStore(sharedPrefs)

        disposable = Observable.interval(3000L, TimeUnit.MILLISECONDS)
                .timeInterval()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { showEnableAdminScreen() }

        val channelId = resources.getString(R.string.notification_channel_id_general)
        val intentNotification = Intent(this, GardionPopupActivity::class.java)
        intentNotification.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        val pendingIntentNotification = PendingIntent.getActivity(applicationContext,
                123, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(this, channelId)
                .setContentTitle(getString(R.string.popup_notification_title))
                .setContentText(getString(R.string.popup_notification_text))
                .setSubText(getString(R.string.popup_notification_subtext))
                .setSmallIcon(R.drawable.ic_notification_warning)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setColor(Color.RED)
                .setContentIntent(pendingIntentNotification)
                .build()

        startForeground(1, notification)
        return START_STICKY
    }

    private fun isDeviceAdminActive(): Boolean {
        return manager.isAdminActive(GardionDeviceAdminReceiver.getComponentName(this))
    }

    private fun showEnableAdminScreen() {
        if (isDeviceAdminActive()) {
            stopSelf()
        } else {
            val launchActivity = Intent(this, GardionPopupActivity::class.java)
            launchActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(launchActivity)
        }
    }

    override fun onBind(bindIntent: Intent?): IBinder {
        return binder
    }

    override fun onDestroy() {
        disposable.dispose()
        super.onDestroy()
    }

    class EnableAdminLocalBinder : Binder() {
        fun getService(): CheckAdminService {
            return CheckAdminService()
        }
    }

}