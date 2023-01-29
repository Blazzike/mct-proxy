package components.regions.packets.server

import packets.BoundTo
import packets.Packet
import packets.PacketInfo
import util.Reader

class PositionPacket(
  var x: Double? = null,
  var feetY: Double? = null,
  var z: Double? = null,
  var onGround: Boolean? = null,
) : Packet() {
  companion object: PacketInfo<PositionPacket>(0x13, BoundTo.SERVER)

  override fun read(reader: Reader): PositionPacket {
    this.x = reader.readDouble()
    this.feetY = reader.readDouble()
    this.z = reader.readDouble()
    this.onGround = reader.readBoolean()

    return this
  }

  override fun additionalParams(): Map<String, Any?> {
    return mapOf(
      "x" to x,
      "feetY" to feetY,
      "z" to z,
      "onGround" to onGround,
    )
  }
}