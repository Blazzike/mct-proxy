package packets.server

import packets.BoundTo
import packets.Packet
import packets.PacketInfo
import packets.PacketState
import util.Reader

class StatusRequest : Packet() {
  companion object: PacketInfo<StatusRequest>(0x00, BoundTo.SERVER, PacketState.STATUS)

  override fun read(reader: Reader): StatusRequest {
    return this
  }
}