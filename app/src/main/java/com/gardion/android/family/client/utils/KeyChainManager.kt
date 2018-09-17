package com.gardion.android.family.client.utils

import android.content.Intent
import android.security.KeyChain
import android.support.v4.app.ActivityCompat.startActivityForResult
import android.util.Base64
import android.util.Log
import com.gardion.android.family.client.toast
import com.gardion.android.family.client.utils.GardionUtils.Companion.makeRequest
import java.io.File
import com.gardion.android.family.client.toast

class KeyChainManager {

    companion object {

        //private val INSTALL_PKCS12 = 0
//
        ////TODO - check if successful - ActivityForResult!
        //private fun importPkcs12() {
        //    try {
        //        val filename = "/sdcard/1.base64"
        //        val input = File(filename).readText()
        //        val pkcs12ByteArray = Base64.decode(input, Base64.DEFAULT)
        //        val intent = KeyChain.createInstallIntent()
        //        intent.putExtra(KeyChain.EXTRA_NAME, "Gardion_Test")
        //        intent.putExtra(KeyChain.EXTRA_PKCS12, pkcs12ByteArray)
        //        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        //        startActivityForResult(intent, INSTALL_PKCS12)
        //    } catch (e: Exception) {
        //        Log.d("GARDION_IMPORT_PKCS12", e.toString())
        //    }
        //}
//
    }
}
