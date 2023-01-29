package components.inventoryTracker.packets.client

import components.inventoryTracker.models.Slot
import packets.BoundTo
import packets.Packet
import packets.PacketInfo
import util.Reader

class SetContainerSlot(
  var windowId: Int? = null,
  var stateId: Int? = null,
  var slotIndex: Short? = null,
  var slot: Slot? = null,
) : Packet() {
  companion object: PacketInfo<SetContainerSlot>(0x12, BoundTo.CLIENT)

  override fun read(reader: Reader): SetContainerSlot {
    windowId = reader.readByte()
    stateId = reader.readVarInt()
    slotIndex = reader.readShort()
    slot = Slot().read(reader)

    return this
  }

  override fun additionalParams(): Map<String, Any?> {
    return mapOf(
      "windowId" to windowId,
      "stateId" to stateId,
      "slotIndex" to slotIndex,
      "slot" to slot,
    )
  }
}