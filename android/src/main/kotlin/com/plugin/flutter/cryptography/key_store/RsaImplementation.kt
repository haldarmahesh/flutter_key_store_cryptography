package com.plugin.flutter.cryptography.key_store

import android.util.Base64
import java.security.*
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

class RsaImplementation {
    private val KEY_FACTORY_ALGORITHM = "RSA"
    private val SIGNATURE_TYPE_ALGORITHM = "SHA256withRSA"

    fun getRsaKeyPair(): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance(KEY_FACTORY_ALGORITHM)
        keyPairGenerator.initialize(2048)
        return keyPairGenerator.generateKeyPair()
    }

    fun sign(plainText: String, rsaPrivateKey: String): String {
        val signature = Signature.getInstance(SIGNATURE_TYPE_ALGORITHM)
        signature.initSign(getPrivateKeyFromString(rsaPrivateKey))
        signature.update(plainText.toByteArray())
        val signedData = signature.sign()
        return Base64.encodeToString(signedData, 0)
    }

    fun getPrivateKeyFromString(base64encodedPrivateKey: String): PrivateKey {
        val encodedKey = Base64.decode(base64encodedPrivateKey, Base64.DEFAULT)
        val keyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM)
        val keySpec = PKCS8EncodedKeySpec(encodedKey)
        return keyFactory.generatePrivate(keySpec)
    }

    fun getPublicKeyFromString(base64encodedPublicKey: String): PublicKey {

        try {
            val publicKeyByteArray = base64encodedPublicKey.toByteArray()
            val encodedPublicKey = Base64.decode(publicKeyByteArray, Base64.DEFAULT)
            val spec = X509EncodedKeySpec(encodedPublicKey)
            val keyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM)
            return keyFactory.generatePublic(spec)
        } catch (e: NoSuchAlgorithmException) {
            throw Exception(ExceptionMessage.NO_ALGORITHM)
        } catch (e: InvalidKeySpecException) {
            throw Exception(ExceptionMessage.INVALID_KEY)
        }

    }

    fun verify(publicKey: String, plainText: String, signature: String): Boolean {
        val sign = Signature.getInstance(SIGNATURE_TYPE_ALGORITHM)
        sign.initVerify(getPublicKeyFromString(publicKey))
        sign.update(plainText.toByteArray())
        return sign.verify(Base64.decode(signature, Base64.DEFAULT))
    }
}
