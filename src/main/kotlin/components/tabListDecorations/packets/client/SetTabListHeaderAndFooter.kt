package components.tabListDecorations.packets.client

import packets.BoundTo
import packets.Packet
import packets.PacketInfo
import util.Buffer

class SetTabListHeaderAndFooter(
  val header: String,
  val footer: String
) : Packet() {
  companion object: PacketInfo<SetTabListHeaderAndFooter>(0x61, BoundTo.CLIENT)

  override fun _write(buffer: Buffer) {
    buffer.writeString(header)
    buffer.writeString(footer)
  }
}