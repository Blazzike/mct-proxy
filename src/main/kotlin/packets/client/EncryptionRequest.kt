package packets.client

import packets.BoundTo
import packets.Packet
import packets.PacketInfo
import packets.PacketState
import util.Buffer
import java.security.PublicKey

class EncryptionRequest(
  private var serverId: String? = null,
  private var publicKey: PublicKey? = null,
  private var verifyToken: ByteArray? = null
) : Packet() {
  companion object: PacketInfo<EncryptionRequest>(0x01, BoundTo.CLIENT, PacketState.LOGIN)

  override fun _write(buffer: Buffer) {
    buffer.writeString(serverId!!)
    buffer.writeVarInt(publicKey!!.encoded.size)
    buffer.writeBytes(publicKey!!.encoded)
    buffer.writeVarInt(verifyToken!!.size)
    buffer.writeBytes(verifyToken)
  }
}