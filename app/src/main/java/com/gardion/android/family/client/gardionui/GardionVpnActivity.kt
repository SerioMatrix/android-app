package com.gardion.android.family.client.gardionui

import android.app.Activity
import android.app.Service
import android.content.*
import android.net.VpnService
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_gardion_vpn.*
import com.gardion.android.family.client.R
import com.gardion.android.family.client.data.datasource.FlowData
import com.gardion.android.family.client.data.datasource.SharedPreferencesDataStore
import com.gardion.android.family.client.network.GardionLinks
import com.gardion.android.family.client.network.GardionNetwork
import com.gardion.android.family.client.network.GardionServerEventManager
import org.strongswan.android.logic.CharonVpnService
import org.strongswan.android.logic.VpnStateService
import org.strongswan.android.logic.VpnStateService.State
import com.gardion.android.family.client.toast
import com.gardion.android.family.client.utils.GardionUtils
import android.content.Intent
import com.gardion.android.family.client.data.datasource.DataStore
import com.gardion.android.family.client.security.GardionRestartService
import java.lang.Exception


class GardionVpnActivity : AppCompatActivity(), VpnStateService.VpnStateListener, GardionPasswordDialog.GardionPasswordDialogListener{

    companion object {
        const val KEY_IS_FROM_BOOT_RECEIVER = "key_is_from_boot_receiver"
        const val KEY_IS_FROM_USER_PRESENT_RECEIVER = "key_is_from_user_present"
        const val KEY_IS_FROM_NETWORK_AVAILABLE = "key_is_from_network_available"
        const val KEY_IS_FROM_DEACTIVATED_VPN = "key_is_from_deactivated_vpn"

        fun getIntent(activity: Activity): Intent {
            return Intent(activity, GardionVpnActivity::class.java)
        }
    }

    private lateinit var flowData: FlowData
    private lateinit var dataStore: DataStore
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
        initButtons()
        handler.removeCallbacks(null)

        val sharedPrefs = this.getSharedPreferences(SharedPreferencesDataStore.PREFERENCES_NAME, Context.MODE_PRIVATE)
        flowData = SharedPreferencesDataStore(sharedPrefs)
        flowData.vpnActivityOpen(true)

        if(!flowData.isGardionDeactivatedAllowed()!!) {
            if (intent.extras != null) {
                if (intent.extras.getBoolean(KEY_IS_FROM_BOOT_RECEIVER, false)) {
                    Log.d("GARDION_CONNECTION", "${this::class.java.simpleName}: $KEY_IS_FROM_BOOT_RECEIVER")
                    startVpnAfterBoot()
                }
                if (intent.extras.getBoolean(KEY_IS_FROM_USER_PRESENT_RECEIVER, false)) {
                    Log.d("GARDION_CONNECTION", "${this::class.java.simpleName}: $KEY_IS_FROM_USER_PRESENT_RECEIVER")
                    startVPNprofile()
                }
                if (intent.extras.getBoolean(KEY_IS_FROM_NETWORK_AVAILABLE, false)) {
                    Log.d("GARDION_CONNECTION", "${this::class.java.simpleName}: $KEY_IS_FROM_NETWORK_AVAILABLE")
                    startVPNprofile()
                }
                if (intent.extras.getBoolean(KEY_IS_FROM_DEACTIVATED_VPN, false)) {
                    Log.d("GARDION_CONNECTION", "${this::class.java.simpleName}: $KEY_IS_FROM_DEACTIVATED_VPN")
                    //startVPNprofile()
                }
            }
            startVPNprofile()
        }
    }

    private fun initButtons() {
        vpn_status_reconnect_button.setOnClickListener { reconnectVpn() }
        vpn_status_disconnect_button.setOnClickListener { tryDisconnectGardionVpn() }
        contact_support_button.setOnClickListener { GardionLinks(this).goToForum() }
        test_event_button.setOnClickListener {sendTestEvent()}
    }

    private fun tryDisconnectGardionVpn() {
        showDialogWithPasswordInput()
    }

    private fun showDialogWithPasswordInput() {
        val gardionDialog = GardionPasswordDialog()
        gardionDialog.show(supportFragmentManager, "fragment_gardion_dialog")
    }

    override fun onFinishEditDialog(inputText: String) {
        val sharedPrefs = this.getSharedPreferences(SharedPreferencesDataStore.PREFERENCES_NAME, Context.MODE_PRIVATE)
        flowData = SharedPreferencesDataStore(sharedPrefs)
        dataStore = SharedPreferencesDataStore(sharedPrefs)
        val hashedInputText = GardionUtils.hashSha256(inputText)
        val savedParentPin = dataStore.getConfigurationParentPin()
        if (hashedInputText == savedParentPin){
            mService?.disconnect()
            toast(getString(R.string.password_toast_gardion_deactivated))
            flowData.gardionDeactivatedAllowed(true)
            flowData.gardionDisabledIllegal(false)
            val eventManager = GardionServerEventManager(this)
            eventManager.sendGardionEvent(GardionServerEventManager.GardionEventType.VPN_DEACTIVATED)
        } else {
            toast(getString(R.string.password_toast_pin_wrong))
        }
    }

    private fun reconnectVpn() {
        val sharedPrefs = this.getSharedPreferences(SharedPreferencesDataStore.PREFERENCES_NAME, Context.MODE_PRIVATE)
        flowData = SharedPreferencesDataStore(sharedPrefs)
        if(GardionUtils.isNetworkAvailable(this)!!)
        {
            flowData.gardionDeactivatedAllowed(false)
            val state: VpnStateService.State? = mService?.state
            when (state) {
                State.CONNECTED, State.CONNECTING -> forceReconnectVpn()
                State.DISCONNECTING, State.DISABLED -> startVPNprofile()
            }
        } else {
            toast(getString(R.string.general_toast_device_offline))
        }
    }

    private fun forceReconnectVpn() {
        toast(getString(R.string.vpn_toast_force_reconnect))
        mService?.disconnect()
        val intent = Intent(this, CharonVpnService::class.java)
        this.startService(intent)
    }

    private fun startVpnAfterBoot() {
        if (GardionUtils.isNetworkAvailable(this)!!) {
            startVPNprofile()
        } else {
            GardionNetwork.requestNetworkInternet(this)
            //if (handlerCounter <= 3) {
            //    handler.postDelayed({ checkInternetConnectionAndIncreaseCounter() }, 10000)
            //}
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
                Log.d("GARDION_CONNECTION", "connected")
            }
            State.DISCONNECTING -> {
                vpn_status_info.text = getString(R.string.vpn_status_disconnecting)
                vpn_status_image.setImageResource(R.drawable.ic_conn_sync)
            }
            State.DISABLED -> {
                vpn_status_info.text = getString(R.string.vpn_status_disabled)
                vpn_status_image.setImageResource(R.drawable.ic_conn_fail)
                Log.d("GARDION_CONNECTION", "disabled")
            }
        }
    }

    override fun stateChanged() {
        updateView()

        val state: VpnStateService.State? = mService?.state
        if(state == State.DISABLED) {
            onDisabled()
        }
        if(state == State.CONNECTED) {
            onConnected()
        }

        if(state == State.CONNECTING) {
            onConnecting()
        }
    }

    private fun onDisabled() {
        //TODO - check if it makes sense to have this flowData = in that many functions or can do only once?
        val sharedPrefs = this.getSharedPreferences(SharedPreferencesDataStore.PREFERENCES_NAME, Context.MODE_PRIVATE)
        flowData = SharedPreferencesDataStore(sharedPrefs)
        Log.d("GARDION_CONNECTION", "onDisabled")
        if(flowData.isGardionDeactivatedAllowed()!!) {
            Log.d("GARDION_CONNECTION", "deactivated allowed")
            flowData.gardionDisabledIllegal(false)
        } else {
            Log.d("GARDION_CONNECTION", "deactivated not allowed will reconnect")
            flowData.gardionDisabledIllegal(true)
            startVPNprofile()
        }
        //TODO - check case when VPN connection is disabled because there is Internet connection
        //TODO - send out event accordingly. done here?. already implmented?
    }

    private fun onConnected() {
        flowData.gardionDisabledIllegal(false)
        stopRestartService()
        val eventManager = GardionServerEventManager(this)
        eventManager.sendGardionEvent(GardionServerEventManager.GardionEventType.VPN_CONNECTED)
    }

    private fun onConnecting() {
        flowData.gardionDisabledIllegal(false)
        stopRestartService()
    }

    private fun stopRestartService() {
        Log.d("GARDION_CONNECTION", "stopRestartService")
        val intent = Intent(this, GardionRestartService::class.java)
        stopService(intent)
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
        val sharedPrefs = this.getSharedPreferences(SharedPreferencesDataStore.PREFERENCES_NAME, Context.MODE_PRIVATE)
        flowData = SharedPreferencesDataStore(sharedPrefs)
        when (requestCode) {
            PREPARE_VPN_SERVICE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val intent = Intent(this, CharonVpnService::class.java)
                    this.startService(intent)
                }

                if(resultCode == Activity.RESULT_CANCELED) {
                    Log.d("GARDION_CONNECTION", "user pressed cancel")
                    if(flowData.isGardionDisabledIllegal()!!) {
                        toast(getString(R.string.vpn_reallow_vpn))
                        startVPNprofile()
                    }
                }
            }
            else -> {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        Log.d("GARDION_CONNECTION", "onStop disabledIllegal ${flowData.isGardionDisabledIllegal()}")
        flowData.vpnActivityOpen(false)
        Log.d("GARDION_Connection", "vpnActivityOpen is set to: ${flowData.isVpnActivityOpen()}")
        if(flowData.isGardionDisabledIllegal()!!){
            bringUpGardion()
        }
   }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("GARDION_CONNECTION", "onDestroy disabledIllegal ${flowData.isGardionDisabledIllegal()}")
        flowData.vpnActivityOpen(false)
        Log.d("GARDION_Connection", "vpnActivityOpen is set to: ${flowData.isVpnActivityOpen()}")
        stopRestartService()
        if(flowData.isGardionDisabledIllegal()!!){
            bringUpGardion()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("GARDION_CONNECTION", "onResume")
        flowData.vpnActivityOpen(true)
        Log.d("GARDION_Connection", "vpnActivityOpen is set to: ${flowData.isVpnActivityOpen()}")
    }

    override fun onStart() {
        super.onStart()
        Log.d("GARDION_CONNECTION", "onStart")
        flowData.vpnActivityOpen(true)
        Log.d("GARDION_Connection", "vpnActivityOpen is set to: ${flowData.isVpnActivityOpen()}")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d("GARDION_CONNECTION", "onRestart")
        flowData.vpnActivityOpen(true)
        Log.d("GARDION_Connection", "vpnActivityOpen is set to: ${flowData.isVpnActivityOpen()}")
    }

    private fun bringUpGardion() {
            try{
                val isGardionRunning = GardionUtils.isMyServiceRunning(GardionRestartService::class.java, this)
                Log.d("GARDION_CONNECTION", "Service runnning: $isGardionRunning")
                if(!isGardionRunning) {
                    val intent = Intent(this, GardionRestartService::class.java)
                    startService(intent)
                }
            } catch (e: Exception) {
                Log.e("GARDION_CONNECTION", e.toString())
            }
    }

    private fun sendTestEvent() {
        val eventManager = GardionServerEventManager(this)
        eventManager.sendGardionEvent(GardionServerEventManager.GardionEventType.TEST_DEV)
    }
}
