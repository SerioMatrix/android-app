package com.gardion.android.family.client.gardionui

import android.app.admin.DevicePolicyManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.gardion.android.family.client.R
import kotlinx.android.synthetic.main.activity_test.*
import android.content.Context
import org.strongswan.android.logic.VpnStateService


class TestActivity : AppCompatActivity() {

    val TAG = "GARDION_TEST"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        button_test123.setOnClickListener {lockDevice()}
    }


    private fun lockDevice() {
        Log.d(TAG, this::class.java.simpleName)
        val mDPM = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        mDPM.lockNow()
    }
}

