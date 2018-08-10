package com.gardion.android.family.client.gardionui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_gardion_login.*
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import com.gardion.android.family.client.R
import com.gardion.android.family.client.data.VpnProfile
import com.gardion.android.family.client.data.VpnProfileDataSource
import com.gardion.android.family.client.data.VpnType
import com.gardion.android.family.client.data.datasource.DataStore
import com.gardion.android.family.client.data.datasource.SharedPreferencesDataStore
import com.gardion.android.family.client.network.GardionApi
import com.gardion.android.family.client.network.model.GardionData
import com.gardion.android.family.client.toast
import java.util.*
import com.gardion.android.family.client.network.GardionMailer


class GardionLoginActivity : AppCompatActivity() {

    companion object {
        private var TAG = GardionLoginActivity::class.java.name

        fun getIntent(activity: Activity): Intent {
            return Intent(activity, GardionLoginActivity::class.java)
        }
    }

    private lateinit var dataStore: DataStore
    private val api: GardionApi = GardionApi.instance
    private var job: Job = Job()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gardion_login)
        val sharedPrefs = this.getSharedPreferences(SharedPreferencesDataStore.PREFERENCES_NAME, Context.MODE_PRIVATE)
        dataStore = SharedPreferencesDataStore(sharedPrefs)
        login_button.setOnClickListener { fetchData() }
        contact_support_button.setOnClickListener { GardionMailer(this).sendMailToSupport() }
    }

    private fun fetchData() {
        val gardionCode = gardion_login_pincode.text.toString()
        job.cancel()
        job = launch(CommonPool) {
            try {
                val response = api.fetchGardionData(gardionCode).execute().body()

                response?.let { saveToDataBase(response) }
                withContext(UI, CoroutineStart.DEFAULT, {
                    toast(getString(R.string.login_toast_success))
                    finishWithData(Activity.RESULT_OK)
                })
            } catch (e: Exception) {
                Log.w(TAG, "Failed to fetch data: " + e.message)
                withContext(UI, CoroutineStart.DEFAULT, {
                    toast(getString(R.string.login_toast_error))
                })
            }
        }
    }

    private fun finishWithData(result: Int) {
        val data = Intent()
        setResult(result, data)
        finish()
    }

    private fun saveToDataBase(gardionData: GardionData) {
        val gardionUrl = gardionData.connection.url[0]
        val gardionUsername = gardionData.connection.authentication.name
        val gardionPassword = gardionData.connection.authentication.password
        val vpnProfile = VpnProfile()
        vpnProfile.name = "gardionVpn"
        vpnProfile.gateway = gardionUrl
        vpnProfile.username = gardionUsername
        vpnProfile.password = gardionPassword
        vpnProfile.uuid = UUID.randomUUID()
        vpnProfile.vpnType = VpnType.IKEV2_EAP
        val database = VpnProfileDataSource(this)
        database.open()
        database.insertProfile(vpnProfile)
        database.close()
        dataStore.setVpnProfileSaved(true)
        saveConfigurationDataToSharedPrefs(gardionData)
    }

    private fun saveConfigurationDataToSharedPrefs(data: GardionData) {
        dataStore.saveConfigurationDeviceId(data.device.id)
        dataStore.saveConfigurationDeviceName(data.device.name)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
