# Flutter key store cryptography

This plugin helps you by generating the assymetric RSA key pair. 

The keys are generated and persisted in android/ios keystore.

This plugin exposes the following methods:
* `getPublicKey`: returns the RSA public key
* `encrypt`: encrypts the plain text using private key from the key pair
* `verify`: verifies a signature and returns a boolean if the signature is signed by the same key pair.
