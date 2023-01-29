package components.inventoryTracker.models

import util.Reader

class Slot(
  var isPresent: Boolean = false,
  var itemStack: ItemStack? = null
) {
  fun read(reader: Reader): Slot {
    isPresent = reader.readBoolean()
    if (isPresent) {
      itemStack = ItemStack.read(reader)
    }

    return this
  }

  override fun toString(): String {
    return "Slot(isPresent=$isPresent, itemStack=$itemStack)"
  }
}