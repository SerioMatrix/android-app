package com.gardion.android.family.client.data.datasource

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
    fun isGardionFirstStart(): Boolean?
    fun gardionFirstStart(firstStart: Boolean)
    fun isUserCertificateChosen(): Boolean?
    fun userCertificateChosen(chosen: Boolean)
    fun isUserCertificateUsed(): Boolean?
    fun userCertificateUsed(certificate: Boolean)
    fun isGardionDeactivatedAllowed(): Boolean?
    fun gardionDeactivatedAllowed(deactivated: Boolean)
    fun isGardionDisabledIllegal(): Boolean?
    fun gardionDisabledIllegal(disabled: Boolean)
    fun isVpnActivityOpen(): Boolean?
    fun vpnActivityOpen(open: Boolean)


}