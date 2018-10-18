package com.gardion.android.family.client.network.model

import com.google.gson.annotations.SerializedName


data class GardionData(
        @SerializedName("desc") val desc: String,
        @SerializedName("version") val version: String,
        @SerializedName("device") val device: Device,
        @SerializedName("group") val group: Group,
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
            @SerializedName("name") val name: String,
            @SerializedName("protocol") val protocol: String,
            @SerializedName("url") val url: List<String>,
            @SerializedName("ip") val ip: List<String>,
            @SerializedName("local_routing") val localRouting: List<String>,
            @SerializedName("authentication") val authentication: Authentication
    ) {

        data class Authentication(
                @SerializedName("auth_type") val authType: String,
                @SerializedName("user_id") val user_id: String,
                @SerializedName("password") val password: String,
                @SerializedName("user_certificate_alias") val userCertificateAlias: String,
                @SerializedName("pkcs12_base64") val pkcs12Base64: String
        )
    }


}