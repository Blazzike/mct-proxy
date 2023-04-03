package components.inventoryTracker.models

import models.UserChannel

class InventorySession(userChannel: UserChannel) {
  var heldIndex: Int? = null
  var inventory = arrayOfNulls<ItemStack>(45)
  var containerOffset: Int? = null
  val heldItem: ItemStack?
    get() = inventory[heldIndex!!]
  val offhandItem: ItemStack?
    get() = inventory[45]

  fun setSlot(slotIndex: Int, itemStack: ItemStack?) {
    inventory[slotIndex] = itemStack
  }

 fun getSlot(slotIndex: Int): ItemStack? {
    return inventory[slotIndex]
  }

  fun setSlots(itemStacks: List<ItemStack?>) {
    inventory = itemStacks.toTypedArray()
  }
}