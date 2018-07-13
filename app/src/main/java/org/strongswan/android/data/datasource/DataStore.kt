package org.strongswan.android.data.datasource

interface DataStore {
    fun saveEncryptedPass(password: String)
    fun getEncryptedPass(): String
    fun setGlobalPasswordSet(passwordCreated: Boolean)
    fun isGlobalPasswordSet(): Boolean?
}