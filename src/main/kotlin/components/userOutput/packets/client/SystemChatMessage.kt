package components.userOutput.packets.client

import packets.BoundTo
import packets.Packet
import packets.PacketInfo
import util.Buffer

class SystemChatMessage(
  val content: String,
  val isActionBar: Boolean
) : Packet() {
  companion object: PacketInfo<SystemChatMessage>(0x60, BoundTo.CLIENT)

  override fun _write(buffer: Buffer) {
    buffer.writeString(content)
    buffer.writeBoolean(isActionBar)
  }
}