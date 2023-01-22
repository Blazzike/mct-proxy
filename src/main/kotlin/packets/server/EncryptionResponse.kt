package packets.server

import packets.BoundTo
import packets.Packet
import packets.PacketInfo
import packets.PacketState
import util.Reader

class EncryptionResponse : Packet() {
  companion object: PacketInfo<EncryptionResponse>(0x01, BoundTo.SERVER, PacketState.LOGIN)

  var sharedSecret: ByteArray? = null
  var verifyToken: ByteArray? = null

  override fun read(reader: Reader): EncryptionResponse {
    sharedSecret = reader.readByteArray()
    verifyToken = reader.readByteArray()

    return this
  }

  override fun additionalParams(): Map<String, Any?> {
    return mapOf(
      "sharedSecret" to sharedSecret,
      "verifyToken" to verifyToken
    )
  }
}