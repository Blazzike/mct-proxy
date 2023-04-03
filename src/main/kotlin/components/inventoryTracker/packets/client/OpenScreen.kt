package components.inventoryTracker.packets.client

import packets.BoundTo
import packets.Packet
import packets.PacketInfo
import util.Reader

class OpenScreen(
  var windowId: Int? = null,
  var windowType: WindowType? = null,
  var windowTitle: String? = null,
) : Packet() {
  enum class WindowType(val inventoryOffset: Int) {
    GENERIC_9X1(9),
    GENERIC_9X2(18),
    GENERIC_9X3(27),
    GENERIC_9X4(36),
    GENERIC_9X5(45),
    GENERIC_9X6(54),
    GENERIC_3X3(9),
    ANVIL(3),
    BEACON(1),
    BLAST_FURNACE(3),
    BREWING_STAND(5),
    CRAFTING_TABLE(10),
    ENCHANTMENT_TABLE(2),
    FURNACE(3),
    GRINDSTONE(2),
    HOPPER(5),
    LECTERN(1),
    LOOM(4),
    MERCHANT(3),
    SHULKER_BOX(27),
    SMITHING_TABLE(3),
    SMOKER(3),
    CARTOGRAPHY_TABLE(2),
    STONECUTTER(1),
  }

  companion object: PacketInfo<OpenScreen>(0x2C, BoundTo.CLIENT)

  override fun read(reader: Reader): OpenScreen {
    windowId = reader.readByte()
    windowType = WindowType.values()[reader.readVarInt()]
    windowTitle = reader.readString()

    return this
  }

  override fun additionalParams(): Map<String, Any?> {
    return mapOf(
      "windowId" to windowId,
      "windowType" to windowType,
      "windowTitle" to windowTitle,
    )
  }
}