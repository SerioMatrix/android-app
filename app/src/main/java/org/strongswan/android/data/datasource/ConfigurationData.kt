package org.strongswan.android.data.datasource

interface ConfigurationData {
    fun saveConfigurationDesc(desc: String)
    fun getConfigurationDesc(): String?
    fun saveConfigurationDeviceId(deviceId: String)
    fun getConfigurationDeviceId(): String?
    fun saveConfigurationDeviceName(deviceName: String)
    fun getConfigurationDeviceName(): String?
}