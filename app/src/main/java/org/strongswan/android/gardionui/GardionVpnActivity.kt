package org.strongswan.android.gardionui

import android.app.Activity
import android.app.Service
import android.content.*
import android.net.VpnService
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_gardion_vpn.*
import org.strongswan.android.R
import org.strongswan.android.data.VpnProfile
import org.strongswan.android.data.VpnProfileDataSource
import org.strongswan.android.data.VpnType
import org.strongswan.android.data.datasource.DataStore
import org.strongswan.android.data.datasource.SharedPreferencesDataStore
import org.strongswan.android.logic.CharonVpnService
import org.strongswan.android.logic.VpnStateService
import org.strongswan.android.logic.VpnStateService.State
import org.strongswan.android.toast
import org.strongswan.android.utils.GardionUtils
import org.strongswan.android.utils.KeyStoreManager
import java.util.*


class GardionVpnActivity : AppCompatActivity(), VpnStateService.VpnStateListener {

    companion object {
        const val PROFILE_REQUIRES_PASSWORD = "org.strongswan.android.MainActivity.REQUIRES_PASSWORD"
        const val PROFILE_NAME = "org.strongswan.android.MainActivity.PROFILE_NAME"
        const val KEY_IS_FROM_BOOT_RECEIVER = "key_is_from_boot_receiver"
        const val KEY_IS_FROM_USER_PRESENT_RECEIVER = "key_is_from_user_present"

        fun getIntent(activity: Activity): Intent {
            return Intent(activity, GardionVpnActivity::class.java)
        }
    }

    private var handlerCounter = 1
    private val handler: Handler = Handler()
    private val PREPARE_VPN_SERVICE = 0
    private var mService: VpnStateService? = null
    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            mService = null
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mService = (service as VpnStateService.LocalBinder).service
            mService?.registerListener(this@GardionVpnActivity)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gardion_vpn)
        applicationContext.bindService(Intent(applicationContext, VpnStateService::class.java),
                mServiceConnection, Service.BIND_AUTO_CREATE)
        if (intent.extras != null) {
            if (intent.extras.getBoolean(KEY_IS_FROM_BOOT_RECEIVER, false)) {
                startVpnAfterBoot()
            }
            if (intent.extras.getBoolean(KEY_IS_FROM_USER_PRESENT_RECEIVER, false)) {
                startVPNprofile()
            }
        }
        vpn_status_dismiss_button.setOnClickListener { dismissActivity() }
        startVPNprofile()
    }

    private fun dismissActivity() {
        onBackPressed()
    }

    private fun startVpnAfterBoot() {
        if (GardionUtils.isInternetConnectionActive(this)) {
            startVPNprofile()
        } else {
            if (handlerCounter <= 3) {
                handler.postDelayed({ checkInternetConnectionAndIncreaseCounter() }, 10000)
            }
        }
    }

    private fun checkInternetConnectionAndIncreaseCounter() {
        if (GardionUtils.isInternetConnectionActive(this)) {
            handler.removeCallbacksAndMessages(null)
            startVPNprofile()
        } else {
            handlerCounter++
        }
    }

    private fun updateView() {
        val state: VpnStateService.State? = mService?.state
        when (state) {
            State.CONNECTING -> {
                vpn_status_info.text = "connecting..."
                vpn_status_image.setImageResource(R.drawable.ic_conn_sync)
            }
            State.CONNECTED -> {
                vpn_status_info.text = "connected"
                vpn_status_image.setImageResource(R.drawable.ic_conn_success)
            }
            State.DISCONNECTING -> {
                vpn_status_info.text = "disconnecting"
                vpn_status_image.setImageResource(R.drawable.ic_conn_sync)
            }
            State.DISABLED -> {
                vpn_status_info.text = "disabled"
                vpn_status_image.setImageResource(R.drawable.ic_conn_fail)
            }
            else -> {
                vpn_status_info.text = "Unknown state"
                vpn_status_image.setImageResource(R.drawable.ic_conn_fail)
            }
        }
    }

    override fun stateChanged() {
        updateView()
    }

    private fun startVPNprofile() {
        val intent: Intent?
        try {
            intent = VpnService.prepare(this)
        } catch (ex: IllegalStateException) {
            this.toast(getString(R.string.vpn_not_supported_during_lockdown))
            return
        } catch (ex: NullPointerException) {
            /* not sure when this happens exactly, but apparently it does */
            this.toast(getString(R.string.vpn_not_supported))
            return
        }
        /* store profile info until the user grants us permission */
        if (intent != null) {
            //TODO: intent jest null, więc nie uruchamia serwisu
            try {
                startActivityForResult(intent, PREPARE_VPN_SERVICE)
            } catch (ex: ActivityNotFoundException) {
                /* it seems some devices, even though they come with Android 4,
				 * don't have the VPN components built into the system image.
				 * com.android.vpndialogs/com.android.vpndialogs.ConfirmDialog
				 * will not be found then */
                this.toast(getString(R.string.vpn_not_supported))
            }

        } else {    /* user already granted permission to use VpnService */
            onActivityResult(PREPARE_VPN_SERVICE, Activity.RESULT_OK, null)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            PREPARE_VPN_SERVICE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val intent = Intent(this, CharonVpnService::class.java)
                    this.startService(intent)
                }
            }
            else -> {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }


}
