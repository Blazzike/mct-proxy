package components.regions.packets.client

import packets.BoundTo
import packets.Packet
import packets.PacketInfo
import util.Buffer

class CommandSuggestionsResponse(
  val id: Int,
  val start: Int,
  val length: Int,
  val suggestions: List<Match>,
) : Packet() {
  class Match(
    val match: String,
    val tooltip: String?,
  )

  companion object: PacketInfo<DisplayObjective>(0x0D, BoundTo.CLIENT)

  override fun _write(buffer: Buffer) {
    buffer.writeVarInt(id)
    buffer.writeVarInt(start)
    buffer.writeVarInt(length)
    buffer.writeArray(suggestions) {
      buffer.writeString(it.match)
      buffer.writeBoolean(it.tooltip != null)
      if (it.tooltip != null) {
        buffer.writeString(it.tooltip)
      }
    }
  }
}