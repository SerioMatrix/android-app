package com.gardion.android.family.client.network

import com.gardion.android.family.client.network.model.GardionData
import com.gardion.android.family.client.network.model.GardionEvent
import kotlinx.android.synthetic.main.imc_state_fragment.view.*
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface GardionApi {

    companion object {
        private const val BASE_URL = "https://api.gardion.net/v1/"

        val instance: GardionApi by lazy {
            val retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            retrofit.create(GardionApi::class.java)
        }
    }

    @GET("configuration/{code}")
    fun fetchGardionData(@Path("code") path: String): Call<GardionData>

    @POST("device/event.json")
    fun postEvent(@Body event: GardionEvent): Call<GardionEvent>
}