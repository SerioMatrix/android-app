package org.strongswan.android.gardionui

import android.content.ComponentName
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_enable_admin.*
import org.strongswan.android.R

class EnableAdminActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enable_admin)
        enable_admin_go_to_settings_button.setOnClickListener { goToDeviceAdminSettings() }
    }

    private fun goToDeviceAdminSettings() {
        startActivity(Intent().setComponent(ComponentName("com.android.settings", "com.android.settings.DeviceAdminSettings")))
    }
}
