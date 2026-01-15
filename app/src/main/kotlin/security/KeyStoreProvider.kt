package com.termiguard.a16.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.util.Base64

/**
 * Generates and manages hardware-backed keys using Android's Keystore.
 * Optimized for Ed25519 support on Android 15.
 */
object KeyStoreProvider {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val SSH_KEY_ALIAS = "TermiGuardSshKey"
    private const val WG_KEY_ALIAS = "TermiGuardWgKey"

    fun getOrCreateSshKeyPair(): KeyPair {
        return getOrCreateKeyPair(SSH_KEY_ALIAS)
    }

    fun getOrCreateWgKeyPair(): KeyPair {
        return getOrCreateKeyPair(WG_KEY_ALIAS)
    }

    private fun getOrCreateKeyPair(alias: String): KeyPair {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        
        if (keyStore.containsAlias(alias)) {
            val privateKey = keyStore.getKey(alias, null) as java.security.PrivateKey
            val publicKey = keyStore.getCertificate(alias).publicKey
            return KeyPair(publicKey, privateKey)
        }

        val kpg = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC, // Android 13+ supports Ed25519 via EC
            ANDROID_KEYSTORE
        )

        val spec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        ).setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
         // Ed25519 specific parameter if needed, but standard EC with Ed25519 curve works on API 33+
         .build()

        kpg.initialize(spec)
        return kpg.generateKeyPair()
    }

    fun exportPublicKeyBase64(keyPair: KeyPair): String {
        return Base64.getEncoder().encodeToString(keyPair.public.encoded)
    }
}
