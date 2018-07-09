package org.strongswan.android.gardionui

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.UserManager
import kotlinx.android.synthetic.main.activity_gardion_main.*
import org.strongswan.android.R
import org.strongswan.android.security.GardionDeviceAdminReceiver
import org.strongswan.android.toast
import java.util.ArrayList
import java.util.HashSet

class GardionMainActivity : AppCompatActivity() {

    private lateinit var manager: DevicePolicyManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gardion_main)
        manager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        main_create_mdm_button.setOnClickListener { createMDM() }
        main_go_to_vpn.setOnClickListener { startVpnActivity() }
        initSwitches()
        show_packagename_textview.text = applicationContext.packageName
        test_button.setOnClickListener { toast("List of Vpn apps: " + getVpnAppList()) }
    }

    private fun initSwitches() {
        val componentName: ComponentName = GardionDeviceAdminReceiver.getComponentName(this)
        switch_always_on.setOnClickListener {
            if (switch_always_on.isChecked) {
                setVpnAlwaysOn(componentName, true)
            } else {
                setVpnAlwaysOn(componentName, false)
            }
        }
        switch_disallow_config_vpn.setOnClickListener {
            if (switch_disallow_config_vpn.isChecked) {
                manager.addUserRestriction(componentName, UserManager.DISALLOW_CONFIG_VPN)
                toast("Disallow config VPN")
            } else {
                manager.clearUserRestriction(componentName, UserManager.DISALLOW_CONFIG_VPN)
                toast("Allow config VPN")
            }
        }
        switch_unable_uninstall.setOnClickListener {
            if (switch_unable_uninstall.isChecked) {
                manager.setUninstallBlocked(componentName, applicationContext.packageName, true)
                toast("Uninstallation Gardion blocked")
            } else {
                manager.setUninstallBlocked(componentName, applicationContext.packageName, false)
                toast("Uninstallation Gardion enabled")
            }
        }
    }

    private fun setVpnAlwaysOn(componentName: ComponentName, enable: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val pkgName: String? = if (enable) {
                applicationContext.packageName
            } else {
                null
            }
            manager.setAlwaysOnVpnPackage(componentName, pkgName , false)
            if (enable) toast("Vpn always ON enabled")
            else toast("Vpn always ON disabled")
        } else {
            toast("error allying policy")
        }
    }

    private fun startVpnActivity() {
        val startVpn = Intent(this, GardionVpnActivity::class.java)
        startActivity(startVpn)
    }

    private fun createMDM() {
        val startMDM = Intent(this, GardionMDMSetupActivity::class.java)
        startActivity(startMDM)
    }

    private fun getVpnAppList(): List<String> {
        val VPN_INTENT = Intent(VpnService.SERVICE_INTERFACE)
        val apps = HashSet<String>()
        val pm = this.packageManager
        val serviceInfos = pm.queryIntentServices(VPN_INTENT, 0)
        for (serviceInfo in serviceInfos) {
            if (serviceInfo.serviceInfo == null) {
                continue
            }
            apps.add(serviceInfo.serviceInfo.packageName)
        }
        return ArrayList(apps)
    }
}
