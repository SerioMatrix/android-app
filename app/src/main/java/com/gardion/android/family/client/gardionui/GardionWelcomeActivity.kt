package com.gardion.android.family.client.gardionui

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.gardion.android.family.client.R
import kotlinx.android.synthetic.main.activity_gardion_welcome.*

class GardionWelcomeActivity : AppCompatActivity() {

    companion object {
        fun getIntent(activity: Activity): Intent {
            return Intent(activity,GardionWelcomeActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gardion_welcome)
        start_initial_setup_button.setOnClickListener{ startInitialSetup() }
    }

    private fun startInitialSetup() {
        finish()
    }
}
