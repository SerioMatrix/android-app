package org.strongswan.android.data.datasource

interface FlowData {
    fun saveEncryptedPass(password: String)
    fun getEncryptedPass(): String
    fun setGlobalPasswordCreated(passwordCreated: Boolean)
    fun isGlobalPasswordCreated(): Boolean?
    fun setVpnProfileSaved(vpnProfileSaved: Boolean)
    fun isVpnProfileSaved(): Boolean?
    fun deviceAdminFirstSet(firstSet: Boolean)
    fun isDeviceAdminFirstSet(): Boolean?
    fun setGardionUnlocked(unlocked: Boolean)
    fun isGardionUnlocked(): Boolean?
}