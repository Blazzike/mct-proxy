package packets.server

import packets.BoundTo
import packets.Packet
import packets.PacketInfo
import packets.PacketState
import util.Buffer
import util.Reader

class Handshake(
  var protocolVersion: Int = 0,
  var serverAddress: String = "",
  var serverPort: Short = 0,
  var nextState: Int = 0
) : Packet() {
  companion object: PacketInfo<Handshake>(0x00, BoundTo.SERVER, PacketState.HANDSHAKE)

  override fun read(reader: Reader): Handshake {
    protocolVersion = reader.readVarInt()
    serverAddress = reader.readString()
    serverPort = reader.readShort()
    nextState = reader.readVarInt()

    return this
  }

  override fun _write(buffer: Buffer) {
    buffer.writeVarInt(protocolVersion)
    buffer.writeString(serverAddress)
    buffer.writeShort(serverPort.toInt())
    buffer.writeVarInt(nextState)
  }

  override fun additionalParams(): Map<String, Any?> {
    return mapOf(
      "protocolVersion" to protocolVersion,
      "serverAddress" to serverAddress,
      "serverPort" to serverPort,
      "nextState" to nextState
    )
  }
}
