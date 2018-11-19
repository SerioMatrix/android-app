package com.gardion.android.family.client.data.datasource

interface ConfigurationData {
    fun saveConfigurationDeviceId(deviceId: String)
    fun getConfigurationDeviceId(): String?
    fun saveConfigurationDeviceName(deviceName: String)
    fun getConfigurationDeviceName(): String?
    fun saveConfigurationPkcs12(deviceName: String)
    fun getConfigurationPkcs12(): String?
    fun saveConfigurationUserCertificateAlias(deviceName: String)
    fun getConfigurationUserCertificateAlias(): String?
    fun saveConfigurationGroupId(groupId: String)
    fun getConfigurationGroupId(): String?
    fun saveConfigurationGroupName(groupName: String)
    fun getConfigurationGroupName(): String?
    fun saveConfigurationParentPin(parentPin: String)
    fun getConfigurationParentPin(): String?
}