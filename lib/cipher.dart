import 'dart:async';

import './call_method_type_enum.dart';
import './cipher_utils.dart';
import './exception_message_constant.dart';
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

  /// sign the data using the private key from keystore
  static Future<String> sign(String plainData) async {
    const String signTag = 'SIGN';
    try {
      final String signedData = await _channel.invokeMethod(
          describeEnum(CallMethodType.encrypt), plainData);
      final formatedSignedData = CipherUtils.removeNewLines(signedData);
      return formatedSignedData;
    } catch (exception) {
      throw PlatformException(
          code: '$_packageTag:$signTag',
          message: ExceptionMessageConstant.signingFailed,
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
