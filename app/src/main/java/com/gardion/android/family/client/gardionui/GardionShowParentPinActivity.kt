package com.gardion.android.family.client.gardionui

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.gardion.android.family.client.R
import kotlinx.android.synthetic.main.activity_gardion_show_parent_pin.*

class GardionShowParentPinActivity : AppCompatActivity() {

    companion object {
        fun getIntent(activity: Activity): Intent {
            return Intent(activity,GardionShowParentPinActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gardion_show_parent_pin)
        val intent = intent
        val parentPin = intent.extras.getString(GardionLoginActivity.INTENT_PARENT_PIN)
        parent_pin_view.text = parentPin
        show_parent_pin_next_button.setOnClickListener {startDeviceAdmin()}
    }

    private fun startDeviceAdmin(){
        finish()
    }
}
