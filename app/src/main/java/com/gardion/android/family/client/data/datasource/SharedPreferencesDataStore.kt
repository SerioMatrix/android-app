package com.gardion.android.family.client.data.datasource

import android.content.SharedPreferences
import com.gardion.android.family.client.get
import com.gardion.android.family.client.set
import org.strongswan.android.utils.StringUtils

class SharedPreferencesDataStore constructor(private val preferences: SharedPreferences) : DataStore {

    companion object {

        const val PREFERENCES_NAME = "gardion_shared_prefs"

        //Gardion flow data
        private const val ENCRYPTED_PASSWORD = "encrypted_password"
        private const val GLOBAL_PASS_SET = "global_pass_set"
        private const val VPN_PROFILE_SAVED = "vpn_profile_saved"
        private const val DEVICE_ADMIN_FIRST_SET = "device_admin_first_set"
        private const val GARDION_APP_UNLOCKED = "gardion_app_unlocked"
        private const val GARDION_FIRST_START = "gardion_first_start"
        private const val USER_CERTIFICATE_CHOSEN = "user_certificate_chosen"
        private const val USER_CERTIFICATE_USED = "user_certificate_used"
        private const val GARDION_DEACTIVATED_ALLOWED = "gardion_deactivated_allowed"
        //Gardion configuration data
        private const val CONFIGURATION_DEVICE_ID = "configuration_device_id"
        private const val CONFIGURATION_DEVICE_NAME = "configuration_device_name"
        private const val CONFIGURATION_PKCS12 = "configuration_pkcs12"
        private const val CONFIGURATION_USER_CERTIFICATE_ALIAS = "configuration_user_certificate_alias"

    }

    override fun saveEncryptedPass(password: String) {
        preferences.set(ENCRYPTED_PASSWORD, password)
    }

    override fun getEncryptedPass(): String {
        return preferences[ENCRYPTED_PASSWORD] ?: StringUtils.EMPTY
    }

    override fun setGlobalPasswordCreated(passwordCreated: Boolean) {
        preferences.set(GLOBAL_PASS_SET, passwordCreated)
    }

    override fun isGlobalPasswordCreated(): Boolean? {
        return preferences[GLOBAL_PASS_SET, false]
    }

    override fun setVpnProfileSaved(vpnProfileSaved: Boolean) {
        preferences.set(VPN_PROFILE_SAVED, vpnProfileSaved)
    }

    override fun isVpnProfileSaved(): Boolean? {
        return preferences[VPN_PROFILE_SAVED, false]
    }

    override fun deviceAdminFirstSet(firstSet: Boolean) {
        preferences.set(DEVICE_ADMIN_FIRST_SET, firstSet)
    }

    override fun isDeviceAdminFirstSet(): Boolean? {
        return preferences[DEVICE_ADMIN_FIRST_SET, true]
    }

    override fun setGardionUnlocked(unlocked: Boolean) {
        preferences.set(GARDION_APP_UNLOCKED, unlocked)
    }

    override fun isGardionUnlocked(): Boolean? {
        return preferences[GARDION_APP_UNLOCKED, false]
    }

    override fun saveConfigurationDeviceId(deviceId: String) {
        preferences.set(CONFIGURATION_DEVICE_ID, deviceId)
    }

    override fun getConfigurationDeviceId(): String? {
        return preferences[CONFIGURATION_DEVICE_ID]
    }

    override fun saveConfigurationDeviceName(deviceName: String) {
        preferences.set(CONFIGURATION_DEVICE_NAME, deviceName)
    }

    override fun getConfigurationDeviceName(): String? {
        return preferences[CONFIGURATION_DEVICE_NAME]
    }

    override fun saveConfigurationPkcs12(pkcs12Base64: String) {
        preferences.set(CONFIGURATION_PKCS12, pkcs12Base64)
    }

    override fun getConfigurationPkcs12(): String? {
        return preferences[CONFIGURATION_PKCS12]
    }

    override fun saveConfigurationUserCertificateAlias(alias: String) {
        preferences.set(CONFIGURATION_USER_CERTIFICATE_ALIAS, alias)
    }

    override fun getConfigurationUserCertificateAlias(): String? {
        return preferences[CONFIGURATION_USER_CERTIFICATE_ALIAS]
    }

    override fun isGardionFirstStart(): Boolean? {
        return preferences[GARDION_FIRST_START, true]
    }

    override fun gardionFirstStart(firstStart: Boolean) {
        preferences.set(GARDION_FIRST_START, firstStart)
    }

    override fun isUserCertificateChosen(): Boolean? {
        return preferences[USER_CERTIFICATE_CHOSEN, false]
    }

    override fun userCertificateChosen(certificateChosen: Boolean) {
        preferences.set(USER_CERTIFICATE_CHOSEN, certificateChosen)
    }

    override fun isUserCertificateUsed(): Boolean? {
        return preferences[USER_CERTIFICATE_USED, false]
    }

    override fun userCertificateUsed(certificate_used: Boolean) {
        preferences.set(USER_CERTIFICATE_USED, certificate_used)
    }

    override fun isGardionDeactivatedAllowed(): Boolean? {
        return preferences[GARDION_DEACTIVATED_ALLOWED]
    }

    override fun gardionDeactivatedAllowed(deactivated: Boolean) {
        preferences.set(GARDION_DEACTIVATED_ALLOWED, deactivated)
    }
}
