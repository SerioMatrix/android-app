package com.gardion.android.family.client.gardionui

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.gardion.android.family.client.R
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
    }

    private fun startDeviceAdmin() {
        finish()
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }
}
