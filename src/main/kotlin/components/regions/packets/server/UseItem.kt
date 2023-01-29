package components.regions.packets.server

import packets.BoundTo
import packets.Packet
import packets.PacketInfo
import util.Reader

class UseItem(
  var hand: Hand? = null,
  var sequence: Int? = null,
  // TODO: more fields from https://wiki.vg/Protocol#Use_Item_On
) : Packet() {
  enum class Hand {
    MAIN,
    OFF,
  }

  companion object: PacketInfo<UseItem>(0x32, BoundTo.SERVER)

  override fun read(reader: Reader): UseItem {
    hand = Hand.values()[reader.readVarInt()]
    sequence = reader.readVarInt()

    return this
  }

  override fun additionalParams(): Map<String, Any?> {
    return mapOf(
      "hand" to hand,
      "sequence" to sequence,
    )
  }
}