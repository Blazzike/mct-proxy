package components.chatComponent.packets.client

import packets.Packet
import packets.PacketInfo
import util.Buffer
import util.Reader
import java.util.*

class SpawnPlayer(
  var entityId: Int? = null,
  var playerUUID: UUID? = null,
  var x: Double? = null,
  var y: Double? = null,
  var z: Double? = null,
  var yaw: Float? = null,
  var pitch: Float? = null,
) : Packet() {
  companion object: PacketInfo<SpawnPlayer>(0x02)

  override fun read(reader: Reader): Packet {
    entityId = reader.readVarInt()
    playerUUID = reader.readUUID()
    x = reader.readDouble()
    y = reader.readDouble()
    z = reader.readDouble()
    yaw = reader.readByte().toFloat()
    pitch = reader.readByte().toFloat()

    return this
  }

  override fun _write(buffer: Buffer) {
    TODO("Not yet implemented")
  }

  override fun additionalParams(): Map<String, Any?> {
    return mapOf(
      "entityId" to entityId,
      "playerUUID" to playerUUID,
      "x" to x,
      "y" to y,
      "z" to z,
      "yaw" to yaw,
      "pitch" to pitch
    )
  }
}

