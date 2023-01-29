package components.regions.packets.server

import packets.BoundTo
import packets.Packet
import packets.PacketInfo
import util.Reader

class SynchronizePlayerPosition(
  var x: Double? = null,
  var y: Double? = null,
  var z: Double? = null,
  var yaw: Float? = null,
  var pitch: Float? = null,
  var flags: Int? = null,
  var teleportId: Int? = null,
  var shouldDismountVehicle: Boolean? = null,
) : Packet() {
  companion object: PacketInfo<SynchronizePlayerPosition>(0x38, BoundTo.CLIENT)

  val isXRelative: Boolean
    get() = flags!! and 0x01 != 0

  val isYRelative: Boolean
    get() = flags!! and 0x02 != 0

  val isZRelative: Boolean
    get() = flags!! and 0x04 != 0

  val isPitchRelative: Boolean
    get() = flags!! and 0x08 != 0

  val isYawRelative: Boolean
    get() = flags!! and 0x10 != 0

  override fun read(reader: Reader): SynchronizePlayerPosition {
    x = reader.readDouble()
    y = reader.readDouble()
    z = reader.readDouble()
    yaw = reader.readFloat()
    pitch = reader.readFloat()
    flags = reader.readByte()
    teleportId = reader.readVarInt()
    shouldDismountVehicle = reader.readBoolean()

    return this
  }

  override fun additionalParams(): Map<String, Any?> {
    return mapOf(
      "x" to x,
      "y" to y,
      "z" to z,
      "yaw" to yaw,
      "pitch" to pitch,
      "flags" to flags,
      "teleportId" to teleportId,
      "shouldDismountVehicle" to shouldDismountVehicle,
    )
  }
}