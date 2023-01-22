package packets.server

import packets.BoundTo
import packets.Packet
import packets.PacketInfo
import packets.PacketState
import util.Buffer
import util.Reader
import java.util.*

class LoginStart(
  var name: String? = null,
  var hasPlayerUUID: Boolean = false,
  var playerUUID: UUID? = null
) : Packet() {
  companion object: PacketInfo<LoginStart>(0x00, BoundTo.SERVER, PacketState.LOGIN)

  override fun read(reader: Reader): LoginStart {
    name = reader.readString()
    hasPlayerUUID = reader.readBoolean()
    if (hasPlayerUUID) {
      playerUUID = reader.readUUID()
    }

    return this
  }

  override fun _write(buffer: Buffer) {
    buffer.writeString(name!!)
    buffer.writeBoolean(hasPlayerUUID)
    if (hasPlayerUUID) {
      buffer.writeUUID(playerUUID!!)
    }
  }

  override fun additionalParams(): Map<String, Any?> {
    return mapOf(
      "name" to name,
      "hasPlayerUUID" to hasPlayerUUID,
      "playerUUID" to playerUUID
    )
  }
}
