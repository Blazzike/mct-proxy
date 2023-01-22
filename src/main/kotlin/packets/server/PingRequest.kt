package packets.server

import packets.Packet
import packets.PacketInfo
import packets.PacketState
import util.Reader

class PingRequest : Packet() {
  companion object: PacketInfo<PingRequest>(0x01, PacketState.STATUS)

  var payload: Long = 0

  override fun read(reader: Reader): PingRequest {
    payload = reader.readLong()

    return this
  }

  override fun additionalParams(): Map<String, Any?> {
    return mapOf("payload" to payload)
  }
}