package com.gardion.android.family.client.network

import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat.startActivity


class GardionMailer(private val context: Context) {
    fun sendMail(recipients : Array<String>, subject: String){
        val email = Intent(Intent.ACTION_SEND)
        email.putExtra(Intent.EXTRA_EMAIL, recipients)
        email.putExtra(Intent.EXTRA_SUBJECT, subject)
        email.type = "message/rfc822"
        email.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(Intent.createChooser(email, "Choose how to send the email"))
    }

    fun sendMailToSupport() {
        val toSupport = arrayOf("support@gardion.com")
        val subject_support = "Gardion: Support Android"
        this.sendMail(toSupport, subject_support)
    }
}
