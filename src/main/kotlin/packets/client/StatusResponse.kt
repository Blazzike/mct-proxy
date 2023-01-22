package packets.client

import org.json.JSONObject
import packets.BoundTo
import packets.Packet
import packets.PacketInfo
import packets.PacketState
import util.Buffer

interface StatusPart {
  fun toJson(): JSONObject
}

class Status(
  val version: Version,
  val players: Players = Players(
    max = 0,
    online = 0,
    sample = emptyList()
  ),
  val description: String = "",
  val favicon: String? = null,
  val previewsChat: Boolean = false,
  val enforcesSecureChat: Boolean = false
) : StatusPart {
  class Version(
    val name: String,
    val protocol: Int
  ) : StatusPart {
    override fun toJson(): JSONObject {
      return JSONObject().apply {
        put("name", name)
        put("protocol", protocol)
      }
    }
  }

  class Player(
    val name: String,
    val id: String
  ) : StatusPart {
    override fun toJson(): JSONObject {
      return JSONObject().apply {
        put("name", name)
        put("id", id)
      }
    }
  }

  class Players(
    val max: Int,
    val online: Int,
    val sample: List<Player>
  ) : StatusPart {
    override fun toJson(): JSONObject {
      return JSONObject().apply {
        put("max", max)
        put("online", online)
        put("sample", sample.map { it.toJson() })
      }
    }
  }

  override fun toJson(): JSONObject {
    return JSONObject().apply {
      put("version", version.toJson())
      put("players", players.toJson())
      put("description", description)
      put("favicon", favicon)
      put("previewsChat", previewsChat)
      put("enforcesSecureChat", enforcesSecureChat)
    }
  }
}

class StatusResponse(private val status: Status) : Packet() {
  companion object: PacketInfo<StatusResponse>(0x00, BoundTo.CLIENT, PacketState.STATUS)
  override fun _write(buffer: Buffer) {
    buffer.writeString(status.toJson().toString())
  }
}