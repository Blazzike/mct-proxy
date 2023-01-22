package packets

import util.Buffer
import util.Reader
import util.Writer
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance

enum class PacketState {
  HANDSHAKE,
  STATUS,
  LOGIN,
  PLAY;

  override fun toString(): String {
    return name.substring(0, 1)
  }
}

open class PacketInfo<P : Packet>(
  val id: Int,
  val state: PacketState = PacketState.PLAY
) {
  val packetClass: KClass<P>
      get() = this::class.java.declaringClass.kotlin as KClass<P>
}

open class Packet {
  companion object: PacketInfo<Packet>(0x00)

  protected open fun _write(buffer: Buffer) {
    TODO("Not yet implemented")
  }

  fun write(writer: Writer) {
    val buffer = Buffer(writer)

    buffer.writeVarInt((this::class.companionObjectInstance as PacketInfo<*>).id)
    _write(buffer)

    writer.writeVarInt(buffer.size)
    buffer.flush()
  }

  open fun additionalParams(): Map<String, Any?> {
    return emptyMap()
  }

  override fun toString(): String {
    val params: HashMap<String, Any?> = HashMap(additionalParams())
    params["id"] = id.toString()

    return "${this::class.simpleName}$params"
  }

  open fun read(reader: Reader): Packet {
    TODO("Not yet implemented")
  }
}

//val packets: Array<Packet> = arrayOf(
//  Handshake(),
//)
//
//fun readPacket(reader: Reader): Packet? {
//  val header = reader.readHeader();
//
//  packets.forEach {
//    if (it.packetId == header.packetId) {
//      return it.read(reader)
//    }
//  }
//
//  reader.inputStream.skipNBytes(header.packetLength.toLong())
//
//  return null
//}
