package org.strongswan.android.gardionui

import android.app.Activity
import android.app.Service
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
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
import org.strongswan.android.logic.CharonVpnService
import org.strongswan.android.logic.VpnStateService
import org.strongswan.android.logic.VpnStateService.State
import org.strongswan.android.toast
import org.strongswan.android.utils.GardionUtils
import org.strongswan.android.utils.KeyStoreManager
import java.util.*


class GardionVpnActivity : AppCompatActivity(), VpnStateService.VpnStateListener {

    companion object {
        val PROFILE_REQUIRES_PASSWORD = "org.strongswan.android.MainActivity.REQUIRES_PASSWORD"
        val PROFILE_NAME = "org.strongswan.android.MainActivity.PROFILE_NAME"
        val KEY_IS_FROM_BOOT_RECEIVER = "key_is_from_boot_receiver"
        val KEY_IS_FROM_USER_PRESENT_RECEIVER = "key_is_from_user_present"
    }

    private var handlerCounter = 1
    private val handler: Handler = Handler()
    private val PREPARE_VPN_SERVICE = 0
    private lateinit var mProfileInfo: Bundle
    private var mProfile: VpnProfile? = VpnProfile()
    private lateinit var mDataSource: VpnProfileDataSource
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
        mDataSource = VpnProfileDataSource(this)
        info_screen_prepare_data.setOnClickListener { saveProfile() }
        info_screen_log_button.setOnClickListener {
            when (mService?.state) {
                State.CONNECTED, State.DISCONNECTING -> disconnectVPN()
                State.DISABLED, State.CONNECTING -> startVPNprofile()
                else -> this.toast("Unknown state")
            }
        }
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

    private fun disconnectVPN() {
        mService?.disconnect()
    }


    private fun updateView() {
        val state: VpnStateService.State? = mService?.state
        when (state) {
            State.CONNECTING -> {
                info_screen_vpn_status.text = "CONNECTING"
                info_screen_progress_bar.visibility = View.VISIBLE
            }
            State.CONNECTED -> {
                info_screen_vpn_status.text = "CONNECTED"
                info_screen_log_button.text = "DISCONNECT"
                info_screen_progress_bar.visibility = View.INVISIBLE
            }
            State.DISCONNECTING -> {
                info_screen_vpn_status.text = "DISCONNECTING"
                info_screen_progress_bar.visibility = View.VISIBLE
            }
            State.DISABLED -> {
                info_screen_vpn_status.text = "DISABLED"
                info_screen_progress_bar.visibility = View.INVISIBLE
            }
            else -> {
                info_screen_vpn_status.text = "UNKNOWN STATE"
                info_screen_progress_bar.visibility = View.INVISIBLE
            }
        }
    }

    private fun saveProfile() {
        info_screen_text.text = "We are saving your credentials"
        info_screen_progress_bar.visibility = View.VISIBLE
        updateProfileData()
        mDataSource.open()
        mDataSource.insertProfile(mProfile)
        if (mProfile?.uuid == null) {
            mProfile?.uuid = UUID.randomUUID()
        }
        mDataSource.updateVpnProfile(mProfile)
        mDataSource.close()
        info_screen_text.text = "Credentials Saved. You can start VPN"
        info_screen_progress_bar.visibility = View.INVISIBLE
    }

    private fun updateProfileData() {
        mProfile?.name = "gardionTest"
        mProfile?.gateway = "vpna.gardion.net"
        mProfile?.vpnType = VpnType.IKEV2_EAP
        mProfile?.username = "joe"
        mProfile?.password = "nlkbl_kZGI8iuzfi7"
        /**
         * Here you can set the spilit tunneling block (IPV4 and IPV6)
         * VpnProfile.SPLIT_TUNNELING_BLOCK_IPV4
         * VpnProfile.SPLIT_TUNNELING_BLOCK_IPV6
         **/
    }

    override fun stateChanged() {
        updateView()
    }

    private fun startVPNprofile() {
        info_screen_text.text = "Preparing connection..."
        info_screen_progress_bar.visibility = View.VISIBLE
        val bundle = prepareInitialData()
        prepareVpnService(bundle)
    }

    private fun getVpnProfile(): VpnProfile? {
        mDataSource.open()
        val vpnProfile: VpnProfile? = mDataSource.getVpnProfile(1)
        mDataSource.close()
        return vpnProfile
    }

    private fun prepareInitialData(): Bundle {
        val bundle = Bundle()
        val vpnProfile: VpnProfile? = getVpnProfile()
        if (vpnProfile != null) {
            mDataSource.open()
            bundle.putLong(VpnProfileDataSource.KEY_ID, vpnProfile.id)
            bundle.putString(VpnProfileDataSource.KEY_USERNAME, vpnProfile.username)
            bundle.putString(VpnProfileDataSource.KEY_PASSWORD, vpnProfile.password)
            mDataSource.close()
        } else {
            bundle.putLong(VpnProfileDataSource.KEY_ID, mProfile!!.id)
            bundle.putString(VpnProfileDataSource.KEY_USERNAME, mProfile?.username)
            bundle.putString(VpnProfileDataSource.KEY_PASSWORD, mProfile?.password)
        }
        bundle.putBoolean(PROFILE_REQUIRES_PASSWORD, true)
        bundle.putString(PROFILE_NAME, "gardion_test")
        return bundle
    }

    private fun prepareVpnService(profileInfo: Bundle) {
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
        mProfileInfo = profileInfo
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            PREPARE_VPN_SERVICE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val intent = Intent(this, CharonVpnService::class.java)
                    intent.putExtras(mProfileInfo)
                    this.startService(intent)
                }
            }
            else -> {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }


}
