package components.inventoryTracker.models

import util.Reader

class ItemStack(
  val itemId: Int,
  val quantity: Int,
) {
  override fun toString(): String {
    return "ItemStack(itemId=$itemId, quantity=$quantity)"
  }

  companion object {
    fun read(reader: Reader): ItemStack {
      val itemStack = ItemStack(
        itemId = reader.readVarInt(),
        quantity = reader.readByte(),
      )

      reader.skipNBT()

      return itemStack
    }
  }
}