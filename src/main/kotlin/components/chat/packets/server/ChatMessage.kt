package components.chat.packets.server

import packets.BoundTo
import packets.Packet
import packets.PacketInfo
import util.Reader

class ChatMessage(
  var message: String? = null,
  var timestamp: Long? = null,
  var salt: Long? = null,
  var signature: ByteArray? = null,
  var messageCount: Int? = null,
  // TODO: acknowledged
) : Packet() {
  companion object: PacketInfo<ChatMessage>(0x05, BoundTo.SERVER)

  override fun read(reader: Reader): ChatMessage {
    message = reader.readString()
    timestamp = reader.readLong()
    salt = reader.readLong()
    if (reader.readBoolean()) {
      signature = reader.readByteArray( 256)
    }

    messageCount = reader.readVarInt()

    return this
  }

  override fun additionalParams(): Map<String, Any?> {
    return mapOf(
      "message" to message,
      "timestamp" to timestamp,
      "salt" to salt,
      "signature" to signature,
      "messageCount" to messageCount,
    )
  }
}