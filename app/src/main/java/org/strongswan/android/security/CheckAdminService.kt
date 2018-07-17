package org.strongswan.android.security

import android.app.Service
import android.content.Intent
import android.os.IBinder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

class CheckAdminService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Observable.interval(3000L, TimeUnit.MILLISECONDS)
                .timeInterval()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { showEnableAdminScreen() }

        return START_STICKY
    }

    private fun showEnableAdminScreen() {

    }

    override fun onBind(bindIntent: Intent?): IBinder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}