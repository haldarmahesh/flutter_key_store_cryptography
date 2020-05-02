# Flutter key store cryptography

This plugin helps you by generating the assymetric RSA key pair. 

The keys are generated and persisted in android/ios keystore.

## Assymetric Key details 
The details of keys generated and store in android/iOS keystore is as follows:
* type -> RSA
* signature algorithm -> SHA256withRSA
* key size -> 2048 bit

### Android
* The RSA key pair is stored android [Keystore](https://developer.android.com/training/articles/keystore.html)
* AES encryption is used.
* The AES secret is encrypted with RSA and stored in shared preference.
* For Android 9 (API leverl 28) [StrongBox](https://developer.android.com/training/articles/keystore.html#HardwareSecurityModule) is enabled by default.
* As keystore is available in Android 4.3 (API level 18) and hgher, hence this is minimum required Android.

### iOS
* The RSA key pair is stored in iOS [Keychain](https://developer.apple.com/documentation/security/keychain_services)
* This plugin will run on iOS 10.0 and higher

## Using plugin
This plugin exposes the following methods:
* `getPublicKey`: returns the RSA public key
* `encrypt`: encrypts the plain text using private key from the key pair
* `verify`: verifies a signature and returns a boolean if the signature is signed by the same key pair.

### 1) getPublicKey
This function is used to get the RSA public key which is store the android and ios key store.
This key pair is persisted in key store, i.e it is generated once, and always re used for signing and verification.

#### defination

```dart
static Future<String> getPublicKey()
```
#### usage: 

```dart
final String rsaPublicKey = await Cipher.getPublicKey();
```

### 2) sign
This function returns a signature which is signed by the platoform's private key.
#### defination

```dart
static Future<String> sign(String plainData)
```

#### usage: 
```dart
final String signature = await Cipher.sign('some plain data');
```

### 3) verify
This function returns a boolean, and takes a plain data and signature.

This function verifies the signature against the plain data, it returns `true` if the signature is produced by the same device's private key.

#### defination

```dart
static Future<bool> verify(String plainText, String signature)
```

#### usage:
```dart
final bool result = await Cipher.verify('somePassword', 'SIGNATURE-XX');
```

The above will checks if the `SIGNATURE-XX` matches the `somePassword` or not. 

