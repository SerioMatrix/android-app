package com.gardion.android.family.client.gardionui

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_password_creator.*
import com.gardion.android.family.client.R
import com.gardion.android.family.client.network.GardionMailer
import com.gardion.android.family.client.network.GardionServerEventManager
import com.gardion.android.family.client.toast

class GardionPasswordCreatorActivity : AppCompatActivity() {

    companion object {
        const val INTENT_EXTRA_PASSWORD: String = "intent_extra_password"

        fun getIntent(activity: Activity): Intent {
            return Intent(activity, GardionPasswordCreatorActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_creator)
        password_create_button.setOnClickListener { createPassword() }
        contact_support_button.setOnClickListener { GardionMailer(this).sendMailToSupport() }
    }

    private fun createPassword() {
        val passwordMain: String = password_editText.text.toString()
        val passwordRepeat: String = password_repeat_editText.text.toString()
        var passwordCreated = false
        when {
            passwordMain.length !in 4..6 -> toast("Your password is too short")
            passwordMain != passwordRepeat -> toast("Typed passwords don't match")
            else -> {
                toast("Password created successfully")
                passwordCreated = true
            }
        }
        if (passwordCreated) {
            finishActivityWithData(passwordMain)
        }
    }

    private fun finishActivityWithData(passwordMain: String) {
        sendPasswordToGardion(passwordMain)
        val data = Intent()
        data.putExtra(INTENT_EXTRA_PASSWORD, passwordMain)
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    private fun sendPasswordToGardion(passwordMain: String) {
        val manager = GardionServerEventManager(this)
        manager.sendPasswordEvent(passwordMain)
    }
}
