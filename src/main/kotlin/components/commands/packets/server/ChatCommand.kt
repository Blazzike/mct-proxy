package components.commands.packets.server

import packets.BoundTo
import packets.Packet
import packets.PacketInfo
import util.Buffer
import util.Reader

class ChatCommand(
  var command: String? = null,
  var timestamp: Long? = null,
  var salt: Long? = null,
  var signatures: List<ArgumentSignature>? = null,
  var messageCount: Int? = null,
  var acknowledged: ByteArray? = null,
) : Packet() {
  class ArgumentSignature(
    val name: String,
    val signature: ByteArray,
  ) {
    override fun toString(): String {
      return "ArgumentSignature(name='$name', signature=${signature.contentToString()})"
    }
  }

  companion object : PacketInfo<ChatCommand>(0x04, BoundTo.SERVER)

  override fun read(reader: Reader): Packet {
    command = reader.readString()
    timestamp = reader.readLong()
    salt = reader.readLong()
    signatures = List(reader.readVarInt()) {
      ArgumentSignature(
        name = reader.readString(),
        signature = reader.readByteArray(256),
      )
    }

    messageCount = reader.readVarInt()
    acknowledged = reader.inputStream.readAllBytes()

    return this
  }

  override fun _write(buffer: Buffer) {
    buffer.writeString(command!!)
    buffer.writeLong(timestamp!!)
    buffer.writeLong(salt!!)
    buffer.writeVarInt(signatures!!.size)
    signatures!!.forEach {
      buffer.writeString(it.name)
      buffer.writeBytes(it.signature)
    }

    buffer.writeVarInt(messageCount!!)
    buffer.writeBytes(acknowledged!!)
  }

  override fun additionalParams(): Map<String, Any?> {
    return mapOf(
      "command" to command,
      "timestamp" to timestamp,
      "salt" to salt,
      "signatures" to signatures,
      "messageCount" to messageCount,
      "acknowledged" to acknowledged,
    )
  }
}