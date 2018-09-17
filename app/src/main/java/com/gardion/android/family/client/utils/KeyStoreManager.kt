package com.gardion.android.family.client.utils

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.*
import javax.crypto.Cipher

class KeyStoreManager {

    companion object {
        private const val GARDION_KEY_ALIAS: String = "Gardion_key"
        private const val KEY_STORE_TYPE: String = "AndroidKeyStore"
        private const val TRANSFORMATION_ASYMMETRIC = "RSA/ECB/PKCS1Padding"

        private val cipher: Cipher = Cipher.getInstance(TRANSFORMATION_ASYMMETRIC)
        private val keyStore: KeyStore = KeyStore.getInstance(KEY_STORE_TYPE).apply { load(null) }

        fun generateKey() {
            if (!keyStore.containsAlias(GARDION_KEY_ALIAS)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val keyGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, KEY_STORE_TYPE)
                    keyGenerator
                            .initialize(KeyGenParameterSpec.Builder(GARDION_KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                                    .setBlockModes(KeyProperties.BLOCK_MODE_ECB)
                                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                                    .build())
                } else {

                }
            }
        }

        fun getAndroidKeyStoreKeyPair(): KeyPair? {
            val privateKey = keyStore.getKey(GARDION_KEY_ALIAS, null) as PrivateKey?
            val publicKey = keyStore.getCertificate(GARDION_KEY_ALIAS)?.publicKey

            return if (privateKey != null && publicKey != null) {
                KeyPair(publicKey, privateKey)
            } else {
                null
            }
        }

        fun encryptData(data: String, key: Key): String {
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val encodedBytes: ByteArray? = cipher.doFinal(data.toByteArray())
            return Base64.encodeToString(encodedBytes, Base64.DEFAULT)
        }

        fun decryptData(encrypted: String, key: Key): String {
            cipher.init(Cipher.DECRYPT_MODE, key)
            val encryptedData = Base64.decode(encrypted, Base64.DEFAULT)
            return String(cipher.doFinal(encryptedData))
        }
    }


}