package packets.client

import packets.BoundTo
import packets.Packet
import packets.PacketInfo
import packets.PacketState
import util.Buffer
import util.Reader
import util.Writer
import java.util.*

public class Property(
  var name: String? = null,
  var value: String? = null,
  var signature: String? = null
) {
  override fun toString(): String {
    return "Property(name=$name, value=$value, signature=$signature)"
  }

  fun write(writer: Writer) {
    writer.writeString(name!!)
    writer.writeString(value!!)
    val isSigned = signature != null
    writer.writeBoolean(isSigned)
    if (isSigned) {
      writer.writeString(signature!!)
    }
  }

  companion object {
    fun read(reader: Reader): Property {
      val name = reader.readString()
      val value = reader.readString()
      val isSigned = reader.readBoolean()
      var signature: String? = null
      if (isSigned) {
        signature = reader.readString()
      }

      return Property(
        name = name,
        value = value,
        signature = signature
      )
    }
  }
}

class LoginSuccess(
  var uuid: UUID? = null,
  var username: String? = null,
  var properties: List<Property> = emptyList()
) : Packet() {
  companion object: PacketInfo<LoginSuccess>(0x02, BoundTo.CLIENT, PacketState.LOGIN)

  override fun _write(buffer: Buffer) {
    buffer.writeUUID(uuid!!)
    buffer.writeString(username!!)
    buffer.writeVarInt(properties.size)
    properties.forEach { it.write(buffer) }
  }

  override fun read(reader: Reader): Packet {
    uuid = reader.readUUID()
    username = reader.readString()
    properties = reader.readArray { Property.read(reader) }

    return this
  }

  override fun additionalParams(): Map<String, Any?> {
    return mapOf(
      "uuid" to uuid,
      "username" to username,
      "properties" to properties
    )
  }
}