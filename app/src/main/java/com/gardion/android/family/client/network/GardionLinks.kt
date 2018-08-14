package com.gardion.android.family.client.network

import android.content.Context
import android.content.Intent
import android.net.Uri


class GardionLinks(private val context: Context) {
    fun goToForum(){
        goToUrl("https://forum.gardion.com/")
    }

    private fun goToUrl(url : String) {
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.addCategory(Intent.CATEGORY_BROWSABLE)
        intent.data = Uri.parse(url)
        context.startActivity(intent)
    }
}
