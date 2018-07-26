package com.gardion.android.family.client.gardionui

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.gardion.android.family.client.R
import com.gardion.android.family.client.security.GardionDeviceAdminReceiver

class EnableProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enable_profile)
        enableProfile()
    }

    private fun enableProfile() {
        val manager: DevicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val componentName: ComponentName = GardionDeviceAdminReceiver.getComponentName(this)
        manager.setProfileName(componentName, "Gardion Managed Profile")
        manager.setProfileEnabled(componentName)
    }
}
