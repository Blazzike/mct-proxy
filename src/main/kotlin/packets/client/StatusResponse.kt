package packets.client

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import packets.BoundTo
import packets.Packet
import packets.PacketInfo
import packets.PacketState
import util.Buffer

interface StatusPart {
  fun toJson(): JsonObject
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
    override fun toJson(): JsonObject {
      return JsonObject().apply {
        addProperty("name", name)
        addProperty("protocol", protocol)
      }
    }
  }

  class Player(
    val name: String,
    val id: String
  ) : StatusPart {
    override fun toJson(): JsonObject {
      return JsonObject().apply {
        addProperty("name", name)
        addProperty("id", id)
      }
    }
  }

  class Players(
    val max: Int,
    val online: Int,
    val sample: List<Player>
  ) : StatusPart {
    override fun toJson(): JsonObject {
      return JsonObject().apply {
        addProperty("max", max)
        addProperty("online", online)
        add("sample", JsonArray().apply {
          sample.forEach {
            add(it.toJson())
          }
        })
      }
    }
  }

  override fun toJson(): JsonObject {
    return JsonObject().apply {
      add("version", version.toJson())
      add("players", players.toJson())
      addProperty("description", description)
      addProperty("favicon", favicon)
      addProperty("previewsChat", previewsChat)
      addProperty("enforcesSecureChat", enforcesSecureChat)
    }
  }
}

class StatusResponse(private val status: Status) : Packet() {
  companion object: PacketInfo<StatusResponse>(0x00, BoundTo.CLIENT, PacketState.STATUS)
  override fun _write(buffer: Buffer) {
    buffer.writeString(status.toJson().toString())
  }
}