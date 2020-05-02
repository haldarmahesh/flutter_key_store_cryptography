import 'dart:async';

import 'package:cipher/call_method_type_enum.dart';
import 'package:cipher/cipher_utils.dart';
import 'package:cipher/exception_message_constant.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

class Cipher {
  static const MethodChannel _channel = MethodChannel('cipher');
  static const _packageTag = 'CIPHER';

  /// get public key from keystore
  static Future<String> getPublicKey() async {
    const String getPublicKeyTag = 'GET_PUBLIC_KEY';
    try {
      final String publicKey = await _channel
          .invokeMethod(describeEnum(CallMethodType.getPublicKey));
      final formatedPublicKey = CipherUtils.removeNewLines(publicKey);
      return formatedPublicKey;
    } catch (exception) {
      throw PlatformException(
          code: '$_packageTag:$getPublicKeyTag',
          message: ExceptionMessageConstant.getPublicKeyFailed,
          details: exception);
    }
  }

  /// encrypt the data using the private key from keystore
  static Future<String> encrypt(String plainData) async {
    const String encryptTag = 'ENCRYPT';
    try {
      final String encryptedData = await _channel.invokeMethod(
          describeEnum(CallMethodType.encrypt), plainData);
      final formatedEncryptedData = CipherUtils.removeNewLines(encryptedData);
      return formatedEncryptedData;
    } catch (exception) {
      throw PlatformException(
          code: '$_packageTag:$encryptTag',
          message: ExceptionMessageConstant.encryptionFailed,
          details: exception);
    }
  }

  /// verify function to verify the app pin against the encrypted signature
  /// [plainText] is the actual value
  /// which needs to be verfied against [signature]
  /// [signature] should be signed by the private from keystore
  static Future<bool> verify(String plainText, String signature) async {
    const String verificationTag = 'VERIFY';
    try {
      final Map<String, String> arguments = {
        'plainText': plainText,
        'signature': signature
      };
      final bool verificationResult = await _channel.invokeMethod(
          describeEnum(CallMethodType.verify), arguments);
      return verificationResult;
    } catch (exception) {
      throw PlatformException(
          code: '$_packageTag:$verificationTag',
          message: ExceptionMessageConstant.verificationFailed,
          details: exception);
    }
  }
}
