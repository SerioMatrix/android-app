package org.strongswan.android.gardionui

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_gardion_ask_for_password.*
import org.strongswan.android.R
import org.strongswan.android.data.datasource.DataStore
import org.strongswan.android.data.datasource.SharedPreferencesDataStore
import org.strongswan.android.toast

class GardionAskForPasswordActivity : AppCompatActivity() {

    private lateinit var dataStore: DataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gardion_ask_for_password)
        ask_pass_unlock_button.setOnClickListener { unlockGardion() }
    }

    private fun unlockGardion() {
        val sharedPrefs = this.getSharedPreferences(SharedPreferencesDataStore.PREFERENCES_NAME, Context.MODE_PRIVATE)
        dataStore = SharedPreferencesDataStore(sharedPrefs)
        val savedPassword = dataStore.getEncryptedPass()
        when (savedPassword) {
            ask_password_type_pass_editText.text.toString() -> unlockTillLockScreen()
            else -> toast("Typed password is incorrect")
        }
    }

    private fun unlockTillLockScreen() {
        dataStore.setGardionUnlocked(true)
        finish()
    }
}
