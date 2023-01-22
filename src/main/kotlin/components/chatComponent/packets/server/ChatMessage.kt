package components.chatComponent.packets.server

import packets.Packet
import packets.PacketInfo
import util.Buffer
import util.Reader

class ChatMessage(
  var message: String? = null,
  var timestamp: Long? = null,
  var salt: Long? = null,
  var hasSignature: Boolean? = false,
  var signature: String? = null,
//  var messageCount: Int? = null, // TODO
//  var acknowledged: BitSet? = null
) : Packet() {
  companion object: PacketInfo<ChatMessage>(0x05)

  override fun read(reader: Reader): Packet {
    message = reader.readString()
    timestamp = reader.readLong()
    salt = reader.readLong()
    hasSignature = reader.readBoolean()
    signature = if (hasSignature!!) reader.readString() else null
//    val messageCount = reader.readVarInt()
//    val acknowledged = reader.readBitSet(messageCount)

    return this
  }

  override fun _write(buffer: Buffer) {
    TODO("Not yet implemented")
  }

  override fun additionalParams(): Map<String, Any?> {
    return mapOf(
      "message" to message,
      "timestamp" to timestamp,
      "salt" to salt,
      "hasSignature" to hasSignature,
      "signature" to signature,
//      "messageCount" to messageCount,
//      "acknowledged" to acknowledged
    )
  }
}

