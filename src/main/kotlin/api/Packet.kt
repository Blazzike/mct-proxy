package api

import packets.Packet
import packets.PacketInfo
import packets.PacketState
import util.Reader
import util.Writer
import kotlin.reflect.full.createInstance

typealias PacketInterceptorCallback<P> = (packet: PacketInterceptorEvent<P>) -> Unit

enum class Direction {
  FROM_SERVER,
  FROM_CLIENT,
}

class PacketInterceptorEvent<P : Packet>(
  val packet: P
) {
  var shouldRewrite = false
  var shouldCancel = false
}

class PacketInterceptor<P : Packet>(
  val direction: Direction,
  val packetInfo: PacketInfo<P>,
  val callback: (packet: PacketInterceptorEvent<*>) -> Unit
)

object PacketInterceptorManager {
  private val fromServer: HashMap<String, Array<PacketInterceptor<*>>> = HashMap()
  private val fromClient: HashMap<String, Array<PacketInterceptor<*>>> = HashMap()

  fun add(packetInterceptor: PacketInterceptor<*>) {
    val packetInfo = packetInterceptor.packetInfo
    val packetIdStr = "${packetInfo.state}${packetInfo.id}"
    val map = when (packetInterceptor.direction) {
      Direction.FROM_SERVER -> fromServer
      Direction.FROM_CLIENT -> fromClient
    }

    if (map[packetIdStr] == null) {
      map[packetIdStr] = arrayOf(packetInterceptor)
    } else {
      map[packetIdStr] = map[packetIdStr]!! + packetInterceptor
    }
  }

  fun handlePacket(
    direction: Direction,
    header: Reader.Header,
    packetState: PacketState,
    packetData: ByteArray,
    writer: Writer
  ): Boolean {
    val map = when (direction) {
      Direction.FROM_SERVER -> fromServer
      Direction.FROM_CLIENT -> fromClient
    }

    val packetInterceptors = map["${packetState}${header.packetId}"] ?: return false

    val reader = Reader(packetData.inputStream())
    val packet = packetInterceptors[0].packetInfo.packetClass
    val packetInstance = packet.createInstance().read(reader)
    val packetInterceptorEvent = PacketInterceptorEvent(packetInstance)
    packetInterceptors.forEach {
      it.callback(packetInterceptorEvent)
    }

    if (packetInterceptorEvent.shouldRewrite && !packetInterceptorEvent.shouldCancel) {
      packetInstance.write(writer)
    }

    return packetInterceptorEvent.shouldRewrite || packetInterceptorEvent.shouldCancel
  }
}

inline fun <reified P : Packet> addPacketInterceptor(direction: Direction, packetInfo: PacketInfo<P>, noinline callback: PacketInterceptorCallback<P>) {
  PacketInterceptorManager.add(PacketInterceptor(
    direction = direction,
    packetInfo = packetInfo,
    callback = callback as (packet: PacketInterceptorEvent<*>) -> Unit
  ))
}