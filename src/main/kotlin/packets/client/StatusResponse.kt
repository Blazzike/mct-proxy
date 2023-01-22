package packets.client

import org.json.JSONObject
import packets.Packet
import packets.PacketInfo
import packets.PacketState
import util.Buffer

class StatusResponse : Packet() {
  companion object: PacketInfo<StatusResponse>(0x00, PacketState.STATUS)

  val json = JSONObject()
  override fun _write(buffer: Buffer) {
    with(json) {
      put("version", JSONObject().apply {
        put("name", "1.19.3")
        put("protocol", 761)
      })

      put("players", JSONObject().apply {
        put("max", 100)
        put("online", 0)
        put("sample", listOf<JSONObject>(
          JSONObject().apply {
            put("name", "Player1")
            put("id", "00000000-0000-0000-0000-000000000000")
          },
          JSONObject().apply {
            put("name", "Player2")
            put("id", "00000000-0000-0000-0000-000000000000")
          }
        ))
      })

      put("description", JSONObject().apply {
        put("text", "A Minecraft Server")
      })

//      put("favicon", "")
      put("previewsChat", true)
      put("enforcesSecureChat", true)

      buffer.writeString(this.toString())
    }
  }
}