
package com.plugin.flutter.cryptography.key_store

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import java.nio.charset.Charset
import java.security.Key
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
class CipherImplementation {

    private val keyStoreCipher: KeyStoreImplementation
    private val rsaImplementation: RsaImplementation

    companion object {
        private val ivSize = 16
        private val keySize = 16
        private const val AES_KEY_ALGORITHM = "AES"
        private val cipher: Cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        private val secureRandom = SecureRandom()
        private var secretKey: Key ? = null
        private var aesKey: String ? = null
        private lateinit var rsaPublicKey: String
        private lateinit var rsaPrivateKey: String
        private var sharedPreferenceName: String = "cipherImplementationKeystore"
        private var SP_PUBLIC_KEY_NAME = "PUBLIC_KEY"
        private var SP_PRIVATE_KEY_NAME = "PRIVATE_KEY"
        private var SP_AES_KEY_NAME = "AES_KEY"
    }

    constructor(context: Context) {
        keyStoreCipher = KeyStoreImplementation(context)
        rsaImplementation = RsaImplementation()
        var sharedPreference: SharedPreferences = context.getSharedPreferences(sharedPreferenceName, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreference.edit()

        /// aes key is generated and stored in shared preference, this key is used to encrypt and decrypt
        aesKey = sharedPreference.getString(SP_AES_KEY_NAME, null)
        val rsaPublicKeyEncrypted = sharedPreference.getString(SP_PUBLIC_KEY_NAME, null)
        val rsaPrivateKeyEncrypted = sharedPreference.getString(SP_PRIVATE_KEY_NAME, null)
        if (aesKey != null) {
            var encrypted: ByteArray
            try {
                encrypted = Base64.decode(aesKey, Base64.DEFAULT)
                secretKey = keyStoreCipher.unwrap(encrypted, AES_KEY_ALGORITHM)
                rsaPublicKey = decryptWithKeyStore(rsaPublicKeyEncrypted)
                rsaPrivateKey = decryptWithKeyStore(rsaPrivateKeyEncrypted)
                // if there is any expection in getting the keystore the exception is caught and
                // new key sets are created
                return
            } catch (exception: Exception) {
                encrypted = ByteArray(0)
            }
        }

        val key = ByteArray(keySize)
        secretKey = SecretKeySpec(key, AES_KEY_ALGORITHM)
        val encryptedKey = keyStoreCipher.wrap(secretKey!!)
        val keyPair = rsaImplementation.getRsaKeyPair()
        secureRandom.nextBytes(key)

        aesKey = Base64.encodeToString(encryptedKey, Base64.DEFAULT)
        // this public and private key is generated to sign the data
        // these keys are encrypted using the keystore and stored in shared preference
        rsaPublicKey = encodeToBase64(keyPair.public.encoded)
        rsaPrivateKey = encodeToBase64(keyPair.private.encoded)

        editor.putString(SP_PUBLIC_KEY_NAME, encryptWithKeystore(rsaPublicKey))
        editor.putString(SP_PRIVATE_KEY_NAME, encryptWithKeystore(rsaPrivateKey))
        editor.putString(SP_AES_KEY_NAME, aesKey)
        editor.commit()
    }

    private fun encodeToBase64(byteArray: ByteArray): String {
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    fun getPublicKey(): String {
        return rsaPublicKey
    }

    fun encrypt(plainInput: String): String {
        return rsaImplementation.sign(plainInput, rsaPrivateKey)
    }

    fun verify(plainText: String, signature: String): Boolean {
        return rsaImplementation.verify(rsaPublicKey, plainText, signature)
    }

    private fun encryptWithKeystore(plainInput: String): String {
        val inputByteArray = plainInput.toByteArray()
        val iv = ByteArray(ivSize)
        secureRandom.nextBytes(iv)
        val ivParameterSpec = IvParameterSpec(iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)
        val payload = cipher.doFinal(inputByteArray)
        val combined = ByteArray(iv.size + payload.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(payload, 0, combined, iv.size, payload.size)
        return encodeToBase64(combined)
    }

    private fun decryptWithKeyStore(input: String): String {
        val iv = ByteArray(ivSize)
        val inputByteArray = Base64.decode(input, 0)
        System.arraycopy(inputByteArray, 0, iv, 0, iv.size)
        val ivParameterSpec = IvParameterSpec(iv)
        val payloadSize = inputByteArray.size - ivSize
        val payload = ByteArray(payloadSize)
        System.arraycopy(inputByteArray, iv.size, payload, 0, payloadSize)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec)
        return String(cipher.doFinal(payload), Charset.forName("UTF-8"))
    }
}
