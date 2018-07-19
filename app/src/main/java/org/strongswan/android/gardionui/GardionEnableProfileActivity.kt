package org.strongswan.android.gardionui

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_gardion_madm_setup.*
import org.strongswan.android.R
import org.strongswan.android.security.GardionDeviceAdminReceiver
import org.strongswan.android.toast

class GardionEnableProfileActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_PROVISION_MANGED_PROFILE: Int = 1
        fun getIntent(activity: Activity): Intent {
            return Intent(activity, GardionEnableProfileActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gardion_madm_setup)
        val manager: DevicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        if(manager.isProfileOwnerApp(applicationContext.packageName)){
            setup_device_admin_button.visibility = View.INVISIBLE
        } else {
            setup_device_admin_button.visibility = View.VISIBLE
        }
        setup_device_admin_button.setOnClickListener { setupProfile() }
    }

    private fun setupProfile() {
        val intent = Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            intent.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_NAME, this.applicationContext.packageName)
        } else {
            val component = ComponentName(this, GardionDeviceAdminReceiver::class.java.name)
            intent.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME, component)
        }

        if (intent.resolveActivity(this.packageManager) != null){
            startActivityForResult(intent, REQUEST_PROVISION_MANGED_PROFILE)
            finish()
        } else {
            toast("Device provisioning is not enabled")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_PROVISION_MANGED_PROFILE) {
            if (resultCode == Activity.RESULT_OK) {
                toast("Provisioning done")
            } else{
                toast("Provisioning failed")
            }
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
