package com.gardion.android.family.client.gardionui

import android.app.Activity
import android.app.Service
import android.content.*
import android.net.VpnService
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_gardion_vpn.*
import com.gardion.android.family.client.R
import com.gardion.android.family.client.data.datasource.FlowData
import com.gardion.android.family.client.data.datasource.SharedPreferencesDataStore
import com.gardion.android.family.client.network.GardionMailer
import org.strongswan.android.logic.CharonVpnService
import org.strongswan.android.logic.VpnStateService
import org.strongswan.android.logic.VpnStateService.State
import com.gardion.android.family.client.toast
import org.strongswan.android.utils.GardionUtils


class GardionVpnActivity : AppCompatActivity(), VpnStateService.VpnStateListener, GardionPasswordDialog.GardionPasswordDialogListener {

    companion object {
        const val KEY_IS_FROM_BOOT_RECEIVER = "key_is_from_boot_receiver"
        const val KEY_IS_FROM_USER_PRESENT_RECEIVER = "key_is_from_user_present"

        fun getIntent(activity: Activity): Intent {
            return Intent(activity, GardionVpnActivity::class.java)
        }
    }

    private lateinit var flowData: FlowData
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
            updateView()
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
        initButtons()
        startVPNprofile()
    }

    private fun initButtons() {
        vpn_status_disconnect_button.setOnClickListener { tryDisconnectGardionVpn() }
        vpn_status_reconnect_button.setOnClickListener { reconnectVpn() }
        contact_support_button.setOnClickListener { GardionMailer(this).sendMailToSupport() }
    }

    private fun tryDisconnectGardionVpn() {
        showDialogWithPasswordInput()
    }

    private fun showDialogWithPasswordInput() {
        val gardionDialog = GardionPasswordDialog()
        gardionDialog.show(supportFragmentManager, "fragment_gardion_dialog")
    }

    override fun onFisnishEditDialog(inputText: String) {
        val sharedPrefs = this.getSharedPreferences(SharedPreferencesDataStore.PREFERENCES_NAME, Context.MODE_PRIVATE)
        flowData = SharedPreferencesDataStore(sharedPrefs)
        val savedPassword = flowData.getEncryptedPass()
        if (inputText == savedPassword){
            mService?.disconnect()
        } else {
            toast("Wrong password")
        }
    }

    private fun reconnectVpn() {
        val state: VpnStateService.State? = mService?.state
        when (state) {
            State.CONNECTED -> forceReconnectVpn()
            State.CONNECTING -> toast("Vpn is trying to connect")
            State.DISCONNECTING, State.DISABLED -> startVPNprofile()
        }
    }

    private fun forceReconnectVpn() {
        toast(getString(R.string.vpn_toast_force_reconnect))
        mService?.disconnect()
        val intent = Intent(this, CharonVpnService::class.java)
        this.startService(intent)
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
                vpn_status_info.text = getString(R.string.vpn_status_connecting)
                vpn_status_image.setImageResource(R.drawable.ic_conn_sync)
            }
            State.CONNECTED -> {
                vpn_status_info.text = getString(R.string.vpn_status_connected)
                vpn_status_image.setImageResource(R.drawable.ic_conn_success)
            }
            State.DISCONNECTING -> {
                vpn_status_info.text = getString(R.string.vpn_status_disconnecting)
                vpn_status_image.setImageResource(R.drawable.ic_conn_sync)
            }
            State.DISABLED -> {
                vpn_status_info.text = getString(R.string.vpn_status_disabled)
                vpn_status_image.setImageResource(R.drawable.ic_conn_fail)
            }
        }
    }

    override fun stateChanged() {
        updateView()
    }

    private fun startVPNprofile() {
        if (!isVpnOn()) {
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
    }

    private fun isVpnOn(): Boolean {
        return GardionUtils.isVpnConnected(this)
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
