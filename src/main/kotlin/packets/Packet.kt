package packets

import api.PacketInterceptorEvent
import api.PacketInterceptorManager
import models.Channel
import util.Buffer
import util.Reader
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

enum class BoundTo {
  CLIENT,
  SERVER,
  NONE,
}

open class PacketInfo<P : Packet>(
  val id: Int,
  val boundTo: BoundTo,
  val state: PacketState = PacketState.PLAY
) {
  val packetClass: KClass<P>
      get() = this::class.java.declaringClass.kotlin as KClass<P>
}

open class Packet {
  companion object: PacketInfo<Packet>(0x00, BoundTo.NONE)

  val packetInfo: PacketInfo<Packet>
    get() = this::class.companionObjectInstance as PacketInfo<Packet>

  protected open fun _write(buffer: Buffer) {
    TODO("Not yet implemented")
  }

  fun write(channel: Channel, isSilent: Boolean = false) {
    val buffer = Buffer(channel.writer)

    buffer.writeVarInt((this::class.companionObjectInstance as PacketInfo<*>).id)
    _write(buffer)

    var handleWrite: PacketInterceptorEvent<*>? = null
    if (!isSilent) {
      handleWrite = PacketInterceptorManager.handleWrite(channel, this)
    }

    if (handleWrite == null || !handleWrite.shouldCancel) {
      channel.writer.writeVarInt(buffer.size)
      buffer.flush()

      handleWrite?.runAfter()
    }
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
