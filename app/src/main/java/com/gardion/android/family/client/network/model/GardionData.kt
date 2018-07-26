package com.gardion.android.family.client.network.model

import com.google.gson.annotations.SerializedName


data class GardionData(
        @SerializedName("desc") val desc: String,
        @SerializedName("version") val version: Double,
        @SerializedName("device") val device: Device,
        @SerializedName("group") val group: Group,
        @SerializedName("messaging") val messaging: Messaging,
        @SerializedName("connection") val connection: Connection
) {

    data class Device(
            @SerializedName("id") val id: String,
            @SerializedName("name") val name: String
    )


    data class Group(
            @SerializedName("id") val id: String,
            @SerializedName("name") val name: String
    )


    data class Connection(
            @SerializedName("protocol") val protocol: String,
            @SerializedName("url") val url: List<String>,
            @SerializedName("local_routing") val localRouting: List<String>,
            @SerializedName("authentication") val authentication: Authentication
    ) {

        data class Authentication(
                @SerializedName("auth_type") val authType: String,
                @SerializedName("name") val name: String,
                @SerializedName("password") val password: String,
                @SerializedName("ca_certs") val caCerts: String,
                @SerializedName("client_cert") val clientCert: String,
                @SerializedName("client-key") val clientKey: String
        )
    }


    data class Messaging(
            @SerializedName("push_id") val pushId: String,
            @SerializedName("forum") val forum: String,
            @SerializedName("email") val email: String
    )
}