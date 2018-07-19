package org.strongswan.android.network

import org.strongswan.android.network.model.GardionData
import org.strongswan.android.network.model.GardionEvent
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

    @POST("event.json")
    fun postEvent(@Body event: GardionEvent): Call<GardionEvent>

}