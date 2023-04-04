package components.commands.packets.server

import packets.BoundTo
import packets.Packet
import packets.PacketInfo
import util.Buffer
import util.Reader

class CommandSuggestionsRequest(
  var transactionId: Int? = null,
  var text: String? = null,
) : Packet() {
  companion object : PacketInfo<CommandSuggestionsRequest>(0x08, BoundTo.SERVER)

  override fun read(reader: Reader): Packet {
    transactionId = reader.readVarInt()
    text = reader.readString()

    return this
  }

  override fun _write(buffer: Buffer) {
    buffer.writeVarInt(transactionId!!)
    buffer.writeString(text!!)
  }

  override fun additionalParams(): Map<String, Any?> {
    return mapOf(
      "transactionId" to transactionId,
      "text" to text,
    )
  }
}