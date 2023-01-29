package components.inventoryTracker.packets.server

import packets.BoundTo
import packets.Packet
import packets.PacketInfo
import util.Reader

class SetHeldItemServerBound(
  var slot: Short? = null,
) : Packet() {
  companion object: PacketInfo<SetHeldItemServerBound>(0x28, BoundTo.SERVER)

  override fun read(reader: Reader): SetHeldItemServerBound {
    slot = reader.readShort()

    return this
  }

  override fun additionalParams(): Map<String, Any?> {
    return mapOf(
      "slot" to slot,
    )
  }
}