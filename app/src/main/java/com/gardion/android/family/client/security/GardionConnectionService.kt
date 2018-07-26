package com.gardion.android.family.client.security

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import com.gardion.android.family.client.network.GardionServerEventManager
import java.util.concurrent.TimeUnit

class GardionConnectionService: Service() {

    private val binder: IBinder = ConnectionLocalBinder()
    private lateinit var disposable: Disposable

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        disposable = Observable.interval(60, TimeUnit.MINUTES)
                .timeInterval()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { postEventActiveVPN() }
        return START_STICKY
    }

    private fun postEventActiveVPN() {
        val eventManager = GardionServerEventManager(this)
        eventManager.sendGardionEvent(GardionServerEventManager.GardionEventType.VPN_INTERVAL_CHECK)
    }

    override fun onBind(p0: Intent?): IBinder {
        return binder
    }

    override fun onDestroy() {
        disposable.dispose()
        super.onDestroy()
    }

    class ConnectionLocalBinder : Binder() {
        fun getService(): GardionConnectionService {
            return GardionConnectionService()
        }
    }
}