package com.gardion.android.family.client.gardionui

import android.content.Context
import android.content.IntentFilter
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.gardion.android.family.client.R
import com.gardion.android.family.client.logic.PackageAddedReceiver
import java.lang.Exception

class PackageAddedTestActivity : AppCompatActivity() {
    private val packageAddedReceiver = PackageAddedReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_package_added_test)


        registerPackageAddedReceiver(this, packageAddedReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterPackageAddedReceiver(this, packageAddedReceiver)
    }



    // TODO - remove just for testing
    private fun registerPackageAddedReceiver(context: Context, par: PackageAddedReceiver) {
        val intentFilter = IntentFilter()
        intentFilter.addAction("android.intent.action.PACKAGE_ADDED")
        context.registerReceiver(par, intentFilter)
        Log.d("GARDION_RECEIVER", "registered receiver Package Added")
    }

    private fun unregisterPackageAddedReceiver(context: Context, par: PackageAddedReceiver) {
        try {
            context.unregisterReceiver(par)
            Log.d("GARDION_RECEIVER", "unregistered receiver Package Added")
        } catch (e: Exception) {
            Log.d("GARDION_RECEIVER", e.toString() + " / no registered receiver Package Added found to unregister")
        }

    }



}
