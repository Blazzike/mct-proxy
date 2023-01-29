package components.inventoryTracker.packets.server

import components.inventoryTracker.models.Slot
import packets.BoundTo
import packets.Packet
import packets.PacketInfo
import util.Reader

class ClickContainer(
  var windowId: Int? = null,
  var stateId: Int? = null,
  var slotIndex: Short? = null,
  var button: Int? = null,
  var mode: InventoryOperationMode? = null,
  var slots: Map<Short, Slot>? = null,
  var carriedItem: Slot? = null
) : Packet() {
  enum class InventoryOperationMode {
    PICKUP,
    QUICK_MOVE,
    SWAP,
    CLONE,
    THROW,
    DISTRIBUTE,
    PICKUP_ALL
  }

  companion object: PacketInfo<ClickContainer>(0x0A, BoundTo.SERVER)

  override fun read(reader: Reader): ClickContainer {
    windowId = reader.readByte()
    stateId = reader.readVarInt()
    slotIndex = reader.readShort()
    button = reader.readByte()
    mode = InventoryOperationMode.values()[reader.readVarInt()]
    val slotsCount = reader.readVarInt()
    slots = Array(slotsCount) {
      reader.readShort() to Slot().read(reader)
    }.toMap()
    println(slots)

    carriedItem = Slot().read(reader)

    return this
  }

  override fun additionalParams(): Map<String, Any?> {
    return mapOf(
      "windowId" to windowId,
      "stateId" to stateId,
      "slotIndex" to slotIndex,
      "button" to button,
      "mode" to mode,
      "slots" to slots,
      "carriedItem" to carriedItem
    )
  }
}