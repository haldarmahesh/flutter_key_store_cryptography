package com.plugin.flutter.cryptography.key_store

class ExceptionMessage {
    companion object {
        const val NO_ALGORITHM = "Could not reconstruct the public key, the given algorithm could not be found."
        const val INVALID_KEY= "Could not reconstruct the public key"
        const val CANNOT_CREATE_KEY = "Cannot create the keys"
        const val CANNOT_CREATE_KEY_WITH_STRONGBOX = "Cannot create keys for Android v 28 and higher, when strongbox is not available"
        const val NOT_PRIVATE_KEY = "Not an instance of private key"
        const val KEY_NOT_FOUND = "No key found with defined alias"
        const val CERTIFICATE_NOT_FOUND = "No certificate found with defined key alias"
    }
}