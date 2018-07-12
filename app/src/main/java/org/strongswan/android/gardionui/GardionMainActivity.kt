package org.strongswan.android.gardionui

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.UserManager
import kotlinx.android.synthetic.main.activity_gardion_main.*
import org.strongswan.android.R
import org.strongswan.android.security.GardionDeviceAdminReceiver
import org.strongswan.android.toast

class GardionMainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gardion_main)
        main_create_mdm_button.setOnClickListener { createMDM() }
        main_go_to_vpn.setOnClickListener { startVpnActivity() }
        val manager: DevicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        main_apply_policies_button.setOnClickListener { applyPolicies(manager) }
        main_clear_policies_button.setOnClickListener { clearPolicies(manager) }
    }

    private fun startVpnActivity() {
        val startVpn = Intent(this, GardionVpnActivity::class.java)
        startActivity(startVpn)
    }

    private fun createMDM() {
        val startMDM = Intent(this, GardionMDMSetupActivity::class.java)
        startActivity(startMDM)
    }

    private fun applyPolicies(manager: DevicePolicyManager) {
        if (manager.isProfileOwnerApp(applicationContext.packageName)) {
            val componentName: ComponentName = GardionDeviceAdminReceiver.getComponentName(this)
            manager.addUserRestriction(componentName, UserManager.DISALLOW_CONFIG_VPN)
            toast("Disallow config vpn")
            manager.addUserRestriction(componentName, UserManager.DISALLOW_UNINSTALL_APPS)
            toast("Disallow uninstall apps")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val config: Bundle = Bundle()
                config.putString("address", "")
                config.putString("identity", "")
                try {
                    manager.setAlwaysOnVpnPackage(componentName, applicationContext.packageName, true)
                    toast("Vpn always on prepared!")
                } catch (e: Exception) {
                    toast("Vpn always on setting failed!")
                }
            }
        } else {
            toast("error allying policies")
        }
    }

    private fun clearPolicies(manager: DevicePolicyManager) {
        if (manager.isProfileOwnerApp(applicationContext.packageName)) {
            val componentName: ComponentName = GardionDeviceAdminReceiver.getComponentName(this)
            manager.clearUserRestriction(componentName, UserManager.DISALLOW_CONFIG_VPN)
            manager.clearUserRestriction(componentName, UserManager.DISALLOW_UNINSTALL_APPS)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                manager.setAlwaysOnVpnPackage(componentName, applicationContext.packageName, false)
            }
            toast("Policies cleared")
        } else {
            toast("Error clearing policies")
        }
    }
}
