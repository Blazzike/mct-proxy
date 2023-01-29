package components.regions.packets.server

import models.Position
import packets.BoundTo
import packets.Packet
import packets.PacketInfo
import util.Reader

class PlayerAction(
  var status: Status? = null,
  var blockPosition: Position? = null,
  var face: Face? = null,
  var sequence: Int? = null,
  // TODO: more fields from https://wiki.vg/Protocol#Use_Item_On
) : Packet() {
  enum class Status(val isDig: Boolean = false) {
    STARTED_DIGGING(true),
    CANCELLED_DIGGING(true),
    FINISHED_DIGGING(true),
    DROP_ITEM_STACK,
    DROP_ITEM,
    SHOOT_ARROW_OR_FINISH_EATING,
    SWAP_ITEM_IN_HAND,
  }

  enum class Face {
    BOTTOM,
    TOP,
    NORTH,
    SOUTH,
    WEST,
    EAST,
  }

  companion object: PacketInfo<PlayerAction>(0x1C, BoundTo.SERVER)

  override fun read(reader: Reader): PlayerAction {
    status = Status.values()[reader.readVarInt()]
    blockPosition = reader.readPosition()
    face = Face.values()[reader.readByte()]
    sequence = reader.readVarInt()

    return this
  }

  override fun additionalParams(): Map<String, Any?> {
    return mapOf(
      "status" to status,
      "blockPosition" to blockPosition,
      "face" to face,
      "sequence" to sequence,
    )
  }
}