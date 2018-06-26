package org.strongswan.android.security

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import org.strongswan.android.data.VpnProfile
import org.strongswan.android.data.VpnProfileDataSource
import org.strongswan.android.gardionui.GardionVpnActivity.Companion.PROFILE_NAME
import org.strongswan.android.gardionui.GardionVpnActivity.Companion.PROFILE_REQUIRES_PASSWORD
import org.strongswan.android.logic.CharonVpnService

class BootCompletedReceiver : BroadcastReceiver() {
    private val TAG: String = "BootCompletedReceiver"
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "Receiver starts")
        val profileInfo: Bundle = getProfileInfoBundle(getVpnDataFromDatabase(context))
        Log.d(TAG, "Profile info fetched from DB$profileInfo")
        if ("android.intent.action.BOOT_COMPLETED" == intent!!.action) {
            val launchVpn = Intent(context, CharonVpnService::class.java)
            intent.putExtras(profileInfo)
            Log.d(TAG, "Service starts")
            context!!.startService(launchVpn)
        }
    }

    private fun getVpnDataFromDatabase(context: Context?): VpnProfile {
        val dataSource = VpnProfileDataSource(context)
        dataSource.open()
        val profile: VpnProfile = dataSource.getVpnProfile(1)
        dataSource.close()
        return profile

    }
    private fun getProfileInfoBundle(vpnData: VpnProfile): Bundle {
        val bundle = Bundle()
        bundle.putLong(VpnProfileDataSource.KEY_ID, vpnData.id)
        bundle.putString(VpnProfileDataSource.KEY_USERNAME, vpnData.username)
        bundle.putString(VpnProfileDataSource.KEY_PASSWORD, vpnData.password)
        bundle.putBoolean(PROFILE_REQUIRES_PASSWORD, true)
        bundle.putString(PROFILE_NAME, "gardion_test")
        return bundle
    }
}