package org.strongswan.android.data.datasource

import android.content.SharedPreferences
import org.strongswan.android.get
import org.strongswan.android.set
import org.strongswan.android.utils.StringUtils

class SharedPreferencesDataStore constructor(private val preferences: SharedPreferences): DataStore {
    companion object {

        const val PREFERENCES_NAME = "gardion_shared_prefs"

        private const val ENCRYPTED_PASSWORD = "encrypted_password"
        private const val GLOBAL_PASS_SET = "global_pass_set"
        private const val VPN_PROFILE_SAVED = "vpn_profile_saved"
        private const val DEVICE_ADMIN_FIRST_SET = "device_admin_first_set"
        private const val GARDION_APP_UNLOCKED = "gardion_app_unlocked"
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
}
