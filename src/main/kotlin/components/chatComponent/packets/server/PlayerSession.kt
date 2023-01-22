package components.chatComponent.packets.server

import packets.Packet
import packets.PacketInfo
import util.Buffer
import util.Reader
import java.util.*

class PlayerSession(
  var sessionId: UUID? = null,
  var expiresAt: Long? = null,
  var publicKey: ByteArray? = null,
  var keySignature: ByteArray? = null
) : Packet() {
  companion object: PacketInfo<PlayerSession>(0x20)

  override fun read(reader: Reader): Packet {
    sessionId = reader.readUUID()
    expiresAt = reader.readLong()
    publicKey = reader.readByteArray()
    keySignature = reader.readByteArray()

    return this
  }

  override fun _write(buffer: Buffer) {
    TODO("Not yet implemented")
  }

  override fun additionalParams(): Map<String, Any?> {
    return mapOf(
      "sessionId" to sessionId,
      "expiresAt" to expiresAt,
      "publicKey" to publicKey,
      "keySignature" to keySignature
    )
  }
}

