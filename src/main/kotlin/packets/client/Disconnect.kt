package packets.client

import packets.BoundTo
import packets.Packet
import packets.PacketInfo
import util.Buffer

class Disconnect(val reason: String) : Packet() {
  companion object: PacketInfo<Disconnect>(0x17, BoundTo.CLIENT)

  override fun _write(buffer: Buffer) {
    buffer.writeString(reason)
  }
}