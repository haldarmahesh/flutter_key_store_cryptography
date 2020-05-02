package com.plugin.flutter.cryptography.key_store

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.StrongBoxUnavailableException
import java.math.BigInteger
import java.security.*
import java.security.spec.AlgorithmParameterSpec
import java.util.*
import javax.crypto.Cipher
import javax.security.auth.x500.X500Principal

class KeyStoreImplementation(context: Context) {
    private val KEY_ALIAS: String
    private val KEYSTORE_PROVIDER_ANDROID = "AndroidKeyStore"
    private val TYPE_RSA = "RSA"
    private val start = Calendar.getInstance()
    private val end = Calendar.getInstance()

    init {
        end.add(Calendar.MONTH, 3) // TODO decide on validity
        KEY_ALIAS = "get_package" + "com.plugin.flutter.cryptography.key_store"
        createKeysIfNotExists(context)
    }

    private fun createKeysIfNotExists(context: Context) {
        val keyStore: KeyStore = KeyStore.getInstance(KEYSTORE_PROVIDER_ANDROID)
        keyStore.load(null)
        val privateKey = keyStore.getKey(KEY_ALIAS, null)
        // if the keys are not present in keystore then create a new pair
        if (privateKey == null)
            createKeys(context)
    }

    private fun createKeys(context: Context) {
        val keyPairGenerator = KeyPairGenerator.getInstance(TYPE_RSA, KEYSTORE_PROVIDER_ANDROID)
        val algorithmParameterSpec: AlgorithmParameterSpec = getAlgorithmSpec(context)

        try {
            initializeKeyPair(keyPairGenerator, algorithmParameterSpec)
        } catch (exception: Exception) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && exception is StrongBoxUnavailableException)
                generateKeysForAndroidPWithoutStrongBox(keyPairGenerator)
             else
                throw Exception(ExceptionMessage.CANNOT_CREATE_KEY)
        }
    }

    @TargetApi(Build.VERSION_CODES.P)
    private fun generateKeysForAndroidPWithoutStrongBox(keyPairGenerator: KeyPairGenerator) {
        try {
            val algorithmParameterSpecWithoutStrongBox = keyPairBuilder().build()
            initializeKeyPair(keyPairGenerator, algorithmParameterSpecWithoutStrongBox)
        } catch (exception: Exception) {
            throw Exception(ExceptionMessage.CANNOT_CREATE_KEY_WITH_STRONGBOX)
        }
    }

    private fun initializeKeyPair(keyPairGenerator: KeyPairGenerator, algorithmParameterSpec: AlgorithmParameterSpec) {
        keyPairGenerator.initialize(algorithmParameterSpec)
        keyPairGenerator.generateKeyPair()
    }

    @TargetApi(Build.VERSION_CODES.P)
    private fun keyPairBuilder(): KeyGenParameterSpec.Builder {

    return KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_DECRYPT or KeyProperties.PURPOSE_ENCRYPT)
                    .setCertificateSubject(X500Principal("CN=$KEY_ALIAS"))
                    .setDigests(KeyProperties.DIGEST_SHA256)
                    .setBlockModes(KeyProperties.BLOCK_MODE_ECB)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                    .setCertificateSerialNumber(BigInteger.valueOf(1))
                    .setCertificateNotBefore(start.time)
                    .setCertificateNotAfter(end.time)
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun keyPairBuilderDeprecated(context: Context): KeyPairGeneratorSpec.Builder {
        return KeyPairGeneratorSpec.Builder(context)
                .setAlias(KEY_ALIAS)
                .setSubject(X500Principal("CN=$KEY_ALIAS"))
                .setSerialNumber(BigInteger.valueOf(1))
                .setStartDate(start.time)
                .setEndDate(end.time)
    }

    private fun getAlgorithmSpec(context: Context): AlgorithmParameterSpec {
        val algorithmParameterSpec: AlgorithmParameterSpec
        if (isAndroidBelowM()) {
            algorithmParameterSpec = keyPairBuilderDeprecated(context).build()
        } else {
            val keyPairSpecBuilder = keyPairBuilder()
            setStrongBox(keyPairSpecBuilder)
            algorithmParameterSpec = keyPairSpecBuilder.build()
        }

        return algorithmParameterSpec
    }
    private fun setStrongBox(keyPairSpecBuilder: KeyGenParameterSpec.Builder) {
        if (isAndroidIsGreaterThanEqualP()) {
            keyPairSpecBuilder.setIsStrongBoxBacked(true)
        }
    }

    private fun isAndroidBelowM(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M
    }

    private fun isAndroidIsGreaterThanEqualP(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
    }

    private fun getPrivateKey(): PrivateKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER_ANDROID)
        keyStore.load(null)
        val key: Key = keyStore.getKey(KEY_ALIAS, null) ?: throw Exception(ExceptionMessage.KEY_NOT_FOUND)
        if (key !is PrivateKey) {
            throw Exception(ExceptionMessage.NOT_PRIVATE_KEY)
        }
        return key
    }

    fun getPublicKey(): PublicKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER_ANDROID)
        keyStore.load(null)

        val certificate = keyStore.getCertificate(KEY_ALIAS) ?: throw Exception(ExceptionMessage.CERTIFICATE_NOT_FOUND)
        val publicKey: PublicKey
        publicKey = certificate.getPublicKey()
        return publicKey
    }

   fun wrap(key: Key): ByteArray {
        val publicKey: PublicKey = getPublicKey()
        val cipher = getRSACipher()
        cipher.init(Cipher.WRAP_MODE, publicKey)
        return cipher.wrap(key)
    }

    fun unwrap(wrappedKey: ByteArray, algorithm: String): Key {
        val privateKey = getPrivateKey()
        val cipher = getRSACipher()
        cipher.init(Cipher.UNWRAP_MODE, privateKey)
        return cipher.unwrap(wrappedKey, algorithm, Cipher.SECRET_KEY)
    }
    // todo with app pin creation
    fun encrypt(input: String): ByteArray {
        val byteArrayInput = input.toByteArray()
        val publicKey = getPublicKey()
        val cipher = getRSACipher()
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        return cipher.doFinal(byteArrayInput)
    }

    @Throws(Exception::class)
    private fun getRSACipher(): Cipher {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // error in android 6: InvalidKeyException: Need RSA private or public key
            Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidOpenSSL")
        } else {
            // error in android 5: NoSuchProviderException: Provider not available: AndroidKeyStoreBCWorkaround
            Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidKeyStoreBCWorkaround")
        }
    }
}
