package packets.client

import packets.Packet
import packets.PacketInfo
import packets.PacketState
import util.Buffer

class PingResponse(private var payload: Long) : Packet() {
  companion object: PacketInfo<PingResponse>(0x01, PacketState.STATUS)

  override fun _write(buffer: Buffer) {
    buffer.writeLong(payload)
  }
}