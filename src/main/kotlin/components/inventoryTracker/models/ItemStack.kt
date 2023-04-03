package components.inventoryTracker.models

import util.Buffer
import util.Reader

class ItemStack(
  val itemId: Int,
  var quantity: Int,
) {
  override fun toString(): String {
    return "ItemStack(itemId=$itemId, quantity=$quantity)"
  }

  fun write(buffer: Buffer) {
    buffer.writeVarInt(itemId)
    buffer.writeByte(quantity)
    buffer.writeByte(0) // NBT skipped
  }

  companion object {
    fun read(reader: Reader): ItemStack {
      val itemStack = ItemStack(
        itemId = reader.readVarInt(),
        quantity = reader.readByte(),
      )

      if (reader.readByte() != 0) {
        reader.inputStream.skipNBytes(2)
        reader.skipNBT(10)
      }

      return itemStack
    }
  }
}