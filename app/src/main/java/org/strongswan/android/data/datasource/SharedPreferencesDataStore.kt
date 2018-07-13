package org.strongswan.android.data.datasource

import android.content.SharedPreferences
import org.strongswan.android.get
import org.strongswan.android.set
import org.strongswan.android.utils.StringUtils

class SharedPreferencesDataStore constructor(private val preferences: SharedPreferences): DataStore {
    companion object {

        private const val ENCRYPTED_PASSWORD = "encrypted_password"
        private const val GLOBAL_PASS_SET = "global_pass_set"

    }
    override fun saveEncryptedPass(password: String) {
        preferences.set(ENCRYPTED_PASSWORD, password)
    }

    override fun getEncryptedPass(): String {
        return preferences[ENCRYPTED_PASSWORD] ?: StringUtils.EMPTY
    }

    override fun setGlobalPasswordSet(passwordCreated: Boolean) {
        preferences.set(GLOBAL_PASS_SET, passwordCreated)
    }

    override fun isGlobalPasswordSet(): Boolean? {
        return preferences[GLOBAL_PASS_SET, false]
    }
}