package components.inventoryTracker.packets.client

import packets.BoundTo
import packets.Packet
import packets.PacketInfo
import util.Reader

class SetHeldItemClientBound(
  var slot: Int? = null,
) : Packet() {
  companion object: PacketInfo<SetHeldItemClientBound>(0x49, BoundTo.CLIENT)

  override fun read(reader: Reader): SetHeldItemClientBound {
    slot = reader.readByte()

    return this
  }

  override fun additionalParams(): Map<String, Any?> {
    return mapOf(
      "slot" to slot,
    )
  }
}