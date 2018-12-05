package com.gardion.android.family.client.gardionui

import android.app.Activity
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.gardion.android.family.client.R
import com.gardion.android.family.client.network.GardionLinks
import kotlinx.android.synthetic.main.activity_gardion_device_admin.*


class GardionAdminActivity : AppCompatActivity() {

    companion object {
        fun getIntent(activity: Activity): Intent {
            return Intent(activity,GardionAdminActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gardion_device_admin)
        device_admin_start_button.setOnClickListener {startDeviceAdmin()}
        if(isInstalledOnExternalStorage()) device_admin_info_external_storage_textView.visibility = View.VISIBLE
        contact_support_button.setOnClickListener { GardionLinks(this).goToForum() }
    }

    private fun startDeviceAdmin() {
        finish()
    }

    private fun isInstalledOnExternalStorage(): Boolean {
        //check if installed on external storage; to set as device admin app must be installed on internal storage
        val pm = packageManager
        val pi = pm.getPackageInfo("com.gardion.android.family.client", 0)
        val ai = pi.applicationInfo
        return ai.flags and ApplicationInfo.FLAG_EXTERNAL_STORAGE == ApplicationInfo.FLAG_EXTERNAL_STORAGE
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }
}
