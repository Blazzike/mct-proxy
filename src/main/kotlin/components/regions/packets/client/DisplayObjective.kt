package components.regions.packets.client

import packets.BoundTo
import packets.Packet
import packets.PacketInfo
import util.Buffer

class DisplayObjective(
  val position: Position,
  val scoreName: String,
) : Packet() {
  enum class Position {
    LIST,
    SIDEBAR,
    BELOW_NAME,
  }

  companion object: PacketInfo<DisplayObjective>(0x4D, BoundTo.CLIENT)

  override fun _write(buffer: Buffer) {
    buffer.writeByte(position.ordinal)
    buffer.writeString(scoreName)
  }
}