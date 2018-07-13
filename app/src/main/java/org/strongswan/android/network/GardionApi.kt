package org.strongswan.android.network

import org.strongswan.android.network.model.GardionData
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface GardionApi {
    @GET("{code}")
    fun fetchGardionData(@Path("code") path: String): Call<GardionData>
}