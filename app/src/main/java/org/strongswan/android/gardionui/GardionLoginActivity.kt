package org.strongswan.android.gardionui

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_gardion_login.*
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import org.strongswan.android.R
import org.strongswan.android.network.GardionApi
import org.strongswan.android.network.model.GardionData
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GardionLoginActivity : AppCompatActivity() {

    companion object {
        private const val BASE_URL = "https://en.gardion.com/api/v1/configuration/"
        private var TAG = GardionLoginActivity::class.java.name
    }

    private val api: GardionApi
    private var job: Job = Job()

    init {
        val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        api = retrofit.create(GardionApi::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gardion_login)
        login_button.setOnClickListener { fetchData() }
    }

    private fun fetchData() {
        val gardionCode: String = gardion_login_pincode.toString()
        job.cancel()
        job = launch(CommonPool) {
            try {
                val response = api.fetchGardionData(gardionCode).execute().body()

                withContext(UI, CoroutineStart.DEFAULT, {
                    val gardionData = response
                    gardionData?.let { saveToDataBase(gardionData) }
                })
            } catch (e: Exception) {
                Log.w(TAG, "Failed to fetch data: "+ e.message)
            }
        }
    }

    private fun saveToDataBase(gardionData: GardionData) {
        val gardionUrl = gardionData.connection.url[1]
        val gardionUsername = gardionData.connection.authentication.name
        val gardionPassword = gardionData.connection.authentication.password
        //TODO: Add those credentials to DataBase
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
