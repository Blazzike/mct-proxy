package api

import models.Channel
import models.User
import packets.Packet
import packets.PacketInfo
import util.Reader
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
    channel: Channel,
    header: Reader.Header
  ) {
    val map = if (channel is User) fromServer else fromClient
    val packetData = channel.reader.inputStream.readNBytes(header.remainingBytes)

    val packetInterceptors = map["${channel.packetState}${header.packetId}"]
    if (packetInterceptors != null) {
      val reader = Reader(packetData.inputStream())
      val packet = packetInterceptors[0].packetInfo.packetClass
      val packetInstance = packet.createInstance().read(reader)
      val packetInterceptorEvent = PacketInterceptorEvent(packetInstance)
      packetInterceptors.forEach {
        it.callback(packetInterceptorEvent)
      }

      if (packetInterceptorEvent.shouldRewrite && !packetInterceptorEvent.shouldCancel) {
        packetInstance.write(channel)

        return
      }
    }

    channel.partner.writer.writeHeader(
      packetId = header.packetId,
      packetLength = header.packetLength
    )

    channel.partner.writer.outputStream.write(packetData)
    channel.partner.writer.outputStream.flush()
  }
}

inline fun <reified P : Packet> addPacketInterceptor(direction: Direction, packetInfo: PacketInfo<P>, noinline callback: PacketInterceptorCallback<P>) {
  PacketInterceptorManager.add(PacketInterceptor(
    direction = direction,
    packetInfo = packetInfo,
    callback = callback as (packet: PacketInterceptorEvent<*>) -> Unit
  ))
}