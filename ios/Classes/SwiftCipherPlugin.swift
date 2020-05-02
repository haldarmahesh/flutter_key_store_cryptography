import Flutter
import UIKit
import CommonCrypto

enum CallMethodTypeEnum: String {
    case getPublicKey
    case encrypt
    case verify
}

enum VerifyArgumentsMapKeysEnum: String {
    case plainText
    case signature
}

public class SwiftCipherPlugin: NSObject, FlutterPlugin {
    let heimdall: Heimdall
    public override init() {
        self.heimdall  =  Heimdall(tagPrefix: "com.plugin.flutter.cryptography.key_store.keys")!
    }
  public static func register(with registrar: FlutterPluginRegistrar) {
    
    let channel = FlutterMethodChannel(name: "cipher", binaryMessenger: registrar.messenger())
    let instance = SwiftCipherPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    
    
    switch(call.method) {
    case CallMethodTypeEnum.getPublicKey.rawValue:
            result(self.getPublicKey())
        break
    case CallMethodTypeEnum.encrypt.rawValue:
            let args = call.arguments
            result(self.sign(plainData: args as! String))
        break
    case CallMethodTypeEnum.verify.rawValue:
            if let args = call.arguments as? Dictionary<String, Any> {
                let plainText = args[VerifyArgumentsMapKeysEnum.plainText.rawValue] as! String
                let signature = args[VerifyArgumentsMapKeysEnum.signature.rawValue] as! String
                result(self.verify(plainText: plainText, signature: signature))
            } else {
                FlutterError(code: "VERIFY_FAIL", message: "Failed to verify signature", details: nil)
            }
        break
    default:
        FlutterError(code: "NO_METHOD", message: "The method \(call.method) is not defined in platform", details: nil)
        break
    }
  }
    
    func getPublicKey() -> String {
        return self.heimdall.getPublicKey()
    }
    
    func sign(plainData: String) -> String {
        let data = plainData.data(using: String.Encoding.utf8)!
        return self.heimdall.sign(data)!.base64EncodedString()
    }
    
    func verify(plainText: String, signature: String) -> Bool {
        let plainData = plainText.data(using: String.Encoding.utf8)!
        guard let signatureData = Data(base64Encoded: signature) else { return false }
        return self.heimdall.verify(plainData, signatureData: signatureData)
    }
    
}
