package components.regions.packets.client

import packets.BoundTo
import packets.Packet
import packets.PacketInfo
import util.Buffer

class UpdateScore(
  val label: String,
  val action: Action,
  val objectiveName: String,
  val value: Int?,
) : Packet() {
  enum class Action {
    CREATE, // OR UPDATE
    REMOVE,
  }

  companion object: PacketInfo<UpdateScore>(0x57, BoundTo.CLIENT)

  override fun _write(buffer: Buffer) {
    buffer.writeString(label)
    buffer.writeVarInt(action.ordinal)
    buffer.writeString(objectiveName)
    if (action != Action.REMOVE) {
      buffer.writeVarInt(value!!)
    }
  }
}