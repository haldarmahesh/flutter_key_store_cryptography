package com.plugin.flutter.cryptography.key_store

import android.util.Log
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar

/** CipherPlugin */
public class CipherPlugin : FlutterPlugin, MethodCallHandler {

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    val channel = MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "cipher")
    channel.setMethodCallHandler(CipherPlugin())
  }

  // This static function is optional and equivalent to onAttachedToEngine. It supports the old
  // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
  // plugin registration via this function while apps migrate to use the new Android APIs
  // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
  //
  // It is encouraged to share logic between onAttachedToEngine and registerWith to keep
  // them functionally equivalent. Only one of onAttachedToEngine or registerWith will be called
  // depending on the user's project. onAttachedToEngine or registerWith must both be defined
  // in the same class.
  companion object {
    private lateinit var cipherImpl: CipherImplementation
    @JvmStatic
    fun registerWith(registrar: Registrar) {
      val channel = MethodChannel(registrar.messenger(), "cipher")
          cipherImpl = CipherImplementation(registrar.context())
      channel.setMethodCallHandler(CipherPlugin())
    }
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    when(call.method) {
      CallMethodTypeEnum.getPublicKey.name -> result.success(cipherImpl.getPublicKey())
      CallMethodTypeEnum.encrypt.name -> result.success(cipherImpl.encrypt(call.arguments as String))
      CallMethodTypeEnum.verify.name -> result.success(verify(call.arguments))
      else -> result.notImplemented()
    }
  }

  private fun verify(arguments: Any): Boolean {
      val arguments: Map<String, String> = arguments as Map<String, String>
      val plainText = arguments[VerifyArgumentsMapKeysEnum.plainText.name] as String
      val signature = arguments[VerifyArgumentsMapKeysEnum.signature.name] as String
      return cipherImpl.verify(plainText, signature)
    }
  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
  }
}
