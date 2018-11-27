package com.gardion.android.family.client.network.model

import com.google.gson.annotations.SerializedName
import java.sql.Timestamp


data class GardionEvent(
        @SerializedName("event") val event: Event
) {

    data class Event(
            @SerializedName("event_timestamp") val eventTimestamp: Long,
            @SerializedName("desc") val desc: String,
            @SerializedName("event_id") val eventId: String,
            @SerializedName("device_id") val deviceId: String,
            @SerializedName("event_payload") val eventPayload: String?
    )
}