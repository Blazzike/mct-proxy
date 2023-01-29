package components.regions.packets.server

import models.Position
import packets.BoundTo
import packets.Packet
import packets.PacketInfo
import util.Reader

class UseItemOn(
  var hand: Hand? = null,
  var position: Position? = null,
  // TODO: more fields from https://wiki.vg/Protocol#Use_Item_On
) : Packet() {
  enum class Hand {
    MAIN,
    OFF,
  }

  companion object: PacketInfo<UseItemOn>(0x31, BoundTo.SERVER)

  override fun read(reader: Reader): UseItemOn {
    hand = Hand.values()[reader.readVarInt()]
    position = reader.readPosition()

    return this
  }

  override fun additionalParams(): Map<String, Any?> {
    return mapOf(
      "hand" to hand,
      "position" to position,
    )
  }
}