package components.tabListDecorations.packets.client

import packets.Packet
import packets.PacketInfo
import packets.PacketState
import util.Buffer

class SetTabListHeaderAndFooter(
  val header: String,
  val footer: String
) : Packet() {
  companion object: PacketInfo<SetTabListHeaderAndFooter>(0x61, PacketState.LOGIN)

  override fun _write(buffer: Buffer) {
    buffer.writeString(header)
    buffer.writeString(footer)
  }
}