package models

import api.Event
import api.EventEmitter
import api.PacketInterceptorManager
import packets.PacketState
import util.Reader
import util.Writer
import java.io.EOFException
import java.net.Socket
import java.net.SocketException

class DisconnectEvent : Event

interface Channel {
  val socket: Socket?
  val writer: Writer
  val reader: Reader
  val packetState: PacketState
  val partner: Channel

  val onDisconnect: EventEmitter<DisconnectEvent>
}

fun Channel.runMirror() {
  while (true) {
    try {
      val header = reader.readHeader()
      PacketInterceptorManager.handlePacket(this, header)
    } catch (e: SocketException) {
      // Client disconnected TODO debug
      break
    } catch (e: EOFException) {
      println("${socket?.inetAddress?.hostAddress ?: "unknown"} disconnected")
      this.onDisconnect.emit(DisconnectEvent())

      // Client disconnected TODO debug
      break
    }
  }
}