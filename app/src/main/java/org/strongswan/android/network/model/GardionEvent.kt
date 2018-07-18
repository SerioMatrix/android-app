package org.strongswan.android.network.model

import com.google.gson.annotations.SerializedName


data class GardionEvent(
        @SerializedName("event") val event: Event
) {

    data class Event(
            @SerializedName("desc") val desc: String,
            @SerializedName("error_id") val errorId: String,
            @SerializedName("device") val device: Device
    ) {

        data class Device(
                @SerializedName("id") val id: String,
                @SerializedName("name") val name: String
        )
    }
}