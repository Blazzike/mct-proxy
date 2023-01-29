package components.regions.packets.client

import packets.BoundTo
import packets.Packet
import packets.PacketInfo
import util.Buffer

class UpdateObjectives(
  val objectiveName: String,
  val mode: Mode,
  val objectiveValue: String? = null,
  val type: Type = Type.INTEGER,
) : Packet() {
  enum class Mode {
    CREATE,
    REMOVE,
    UPDATE,
  }

  enum class Type {
    INTEGER,
    HEARTS,
  }

  companion object: PacketInfo<UpdateObjectives>(0x54, BoundTo.CLIENT)

  override fun _write(buffer: Buffer) {
    buffer.writeString(objectiveName)
    buffer.writeByte(mode.ordinal)
    if (mode != Mode.REMOVE) {
      buffer.writeString(objectiveValue!!)
      buffer.writeByte(type.ordinal)
    }
  }
}