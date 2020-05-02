 //
 //  SimpleASN1Writer.swift
 //  simple-asn1-reader-writer
 //
 //  Created by nextincrement on 27/07/2019.
 //  Copyright © 2019 nextincrement
 //
 import Foundation
 
public final class SimpleASN1Writer {
 
   // Constants
   private let bitStringIdentifier: UInt8 = 0x03
   private let supportedFirstContentsByte: UInt8 = 0x00
 
   // Instance variable
   public private(set) var encoding: [UInt8] = []
 
   public init() {}
 
   public func write(from writer: SimpleASN1Writer) {
     encoding.insert(contentsOf: writer.encoding, at: 0)
   }
 
   public func write(_ bytes: [UInt8]) {
     encoding.insert(contentsOf: bytes, at: 0)
   }
 
   public func write(_ contents: [UInt8], identifiedBy identifier: UInt8) {
     encoding.insert(contentsOf: contents, at: 0)
     writeLengthAndIdentifier(with: identifier, onTopOf: contents)
   }
 
   public func wrap(with identifier: UInt8) {
     writeLengthAndIdentifier(with: identifier, onTopOf: encoding)
   }
 
   public func wrapBitString() {
     encoding.insert(supportedFirstContentsByte, at: 0)
     writeLengthAndIdentifier(with: bitStringIdentifier, onTopOf: encoding)
   }
 
   private func writeLengthAndIdentifier(with identifier: UInt8, onTopOf contents: [UInt8]) {
     encoding.insert(contentsOf: lengthField(of: contents), at: 0)
     encoding.insert(identifier, at: 0)
   }
 
   private func lengthField(of contentBytes: [UInt8]) -> [UInt8] {
     let length = contentBytes.count
 
     if length < 128 {
       return [UInt8(length)]
     }
     return longLengthField(of: contentBytes)
   }
 
   private func longLengthField(of contentBytes: [UInt8]) -> [UInt8] {
     var length = contentBytes.count
 
     // Number of bytes needed to encode the length
     let lengthFieldCount = Int((log2(Double(length)) / 8) + 1)
     var lengthField: [UInt8] = []
 
     for _ in 0..<lengthFieldCount {
 
       // Take the last 8 bits of length
       let lengthByte = UInt8(length & 0xff)
 
       // Insert them at the beginning of the array
       lengthField.insert(lengthByte, at: 0)
 
       // Delete the last 8 bits of length
       length = length >> 8
     }
     let firstByte = UInt8(128 + lengthFieldCount)
 
     // Insert first byte at the beginning of the array
     lengthField.insert(firstByte, at: 0)
 
     return lengthField
   }
}
