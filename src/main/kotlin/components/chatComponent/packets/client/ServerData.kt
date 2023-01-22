package components.chatComponent.packets.client

import packets.Packet
import packets.PacketInfo
import util.Buffer
import util.Reader

class ServerData(
  var hasMOTD: Boolean? = null,
  var MOTD: String? = null,
  var hasIcon: Boolean? = null,
  var icon: String? = null,
  var enforcesSecureChat: Boolean? = null,
) : Packet() {
  companion object: PacketInfo<ServerData>(0x41)

  override fun read(reader: Reader): Packet {
    hasMOTD = reader.readBoolean()
    if (hasMOTD!!) {
      MOTD = reader.readString()
    }
    hasIcon = reader.readBoolean()
    if (hasIcon!!) {
      icon = reader.readString()
    }
    enforcesSecureChat = reader.readBoolean()

    return this
  }

  override fun _write(buffer: Buffer) {
    TODO("Not yet implemented")
  }

  override fun additionalParams(): Map<String, Any?> {
    return mapOf(
      "hasMOTD" to hasMOTD,
      "MOTD" to MOTD,
      "hasIcon" to hasIcon,
      "icon" to icon,
      "enforcesSecureChat" to enforcesSecureChat,
    )
  }
}

