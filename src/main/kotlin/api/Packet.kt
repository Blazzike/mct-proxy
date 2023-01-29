package api

import models.Channel
import models.UserChannel
import models.VanillaChannel
import packets.BoundTo
import packets.Packet
import packets.PacketInfo
import util.Reader
import kotlin.reflect.full.createInstance

typealias PacketInterceptorCallback<P> = (packet: PacketInterceptorEvent<P>) -> Unit

class PacketInterceptorEvent<P : Packet>(
  var packet: P,
  val userChannel: UserChannel
) {
  var shouldRewrite = false
  var shouldCancel = false

  var afters: List<(() -> Unit)> = emptyList()
  fun after(function: () -> Unit) {
    afters += function
  }

  fun runAfter() {
    afters.forEach { it() }
  }

  override fun toString(): String {
    return "PacketInterceptorEvent(packet=$packet, user=$userChannel, shouldRewrite=$shouldRewrite, shouldCancel=$shouldCancel, afters=$afters)"
  }
}

class PacketInterceptor<P : Packet>(
  val packetInfo: PacketInfo<P>,
  val callback: (packet: PacketInterceptorEvent<*>) -> Unit
) {
  override fun toString(): String {
    return "PacketInterceptor(packetInfo=$packetInfo, callback=$callback)"
  }
}

object PacketInterceptorManager {
  private val fromServer: HashMap<String, Array<PacketInterceptor<*>>> = HashMap()
  private val fromClient: HashMap<String, Array<PacketInterceptor<*>>> = HashMap()

  fun add(packetInterceptor: PacketInterceptor<*>) {
    val packetInfo = packetInterceptor.packetInfo
    val packetIdStr = "${packetInfo.state}${packetInfo.id}"
    val map = when (packetInfo.boundTo) {
      BoundTo.CLIENT -> fromServer
      BoundTo.SERVER -> fromClient
      else -> throw IllegalArgumentException("PacketInterceptor must be bound to either client or server")
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
    val map = if (channel is VanillaChannel) fromServer else fromClient
    val packetData = channel.reader.inputStream.readNBytes(header.remainingBytes)
    val packetInterceptors = map["${channel.packetState}${header.packetId}"]
    if (packetInterceptors != null) {
      val reader = Reader(packetData.inputStream())
      val packet = packetInterceptors[0].packetInfo.packetClass
      val packetInstance = packet.createInstance().read(reader)
      val packetInterceptorEvent = PacketInterceptorEvent(
        packetInstance,
        if (channel is UserChannel) channel else channel.partner as UserChannel
      )

      packetInterceptors.forEach {
        it.callback(packetInterceptorEvent)
      }

      if (packetInterceptorEvent.shouldCancel) {
        return
      }

      if (packetInterceptorEvent.shouldRewrite) {
        packetInterceptorEvent.packet.write(channel.partner, true)

        packetInterceptorEvent.runAfter()

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

  fun <P : Packet> handleWrite(channel: Channel, packet: P): PacketInterceptorEvent<P>? {
    val map = if (channel is UserChannel) fromServer else fromClient
    val packetInterceptors = map["${channel.packetState}${packet.packetInfo.id}"]
    if (packetInterceptors != null) {
      val packetInterceptorEvent = PacketInterceptorEvent(packet, channel as UserChannel)
      packetInterceptors.forEach {
        it.callback(packetInterceptorEvent)
      }

      if (packetInterceptorEvent.shouldCancel) {
        return packetInterceptorEvent
      }

      if (packetInterceptorEvent.shouldRewrite) {
        packetInterceptorEvent.shouldCancel = true
        packetInterceptorEvent.packet.write(channel, true)
        packetInterceptorEvent.runAfter()
      }

      return packetInterceptorEvent
    }

    return null
  }
}

inline fun <reified P : Packet> addPacketInterceptor(packetInfo: PacketInfo<P>, noinline callback: PacketInterceptorCallback<P>) {
  PacketInterceptorManager.add(PacketInterceptor(
    packetInfo = packetInfo,
    callback = callback as (packet: PacketInterceptorEvent<*>) -> Unit
  ))
}