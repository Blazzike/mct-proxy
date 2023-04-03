package components.inventoryTracker.packets.client

import components.inventoryTracker.models.Slot
import packets.BoundTo
import packets.Packet
import packets.PacketInfo
import util.Buffer
import util.Reader

class SetContainerContent(
  var windowId: Int? = null,
  var stateId: Int? = null,
  var slotData: List<Slot>? = null,
  var carriedItem: Slot? = null,
) : Packet() {
  companion object: PacketInfo<SetContainerContent>(0x10, BoundTo.CLIENT)

  override fun read(reader: Reader): SetContainerContent {
    windowId = reader.readByte()
    stateId = reader.readVarInt()
    slotData = reader.readArray { Slot().read(reader) }
    carriedItem = Slot().read(reader)

    return this
  }

  override fun _write(buffer: Buffer) {
    buffer.writeByte(windowId!!)
    buffer.writeVarInt(stateId!!)
    buffer.writeArray(slotData!!) { it.write(buffer) }
    carriedItem!!.write(buffer)
  }

  override fun additionalParams(): Map<String, Any?> {
    return mapOf(
      "windowId" to windowId,
      "stateId" to stateId,
      "slotData" to slotData,
      "carriedItem" to carriedItem,
    )
  }
}