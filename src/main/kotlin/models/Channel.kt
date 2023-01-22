package models

import api.PacketInterceptorManager
import packets.PacketState
import util.Reader
import util.Writer
import java.net.Socket
import java.net.SocketException

interface Channel {
  val socket: Socket
  val writer: Writer
  val reader: Reader
  val packetState: PacketState
  val partner: Channel
}

fun Channel.runMirror() {
  while (true) {
    try {
      val header = reader.readHeader()
      PacketInterceptorManager.handlePacket(this, header)
    } catch (e: SocketException) {
      break
    } catch (e: EOFException) {
      break
    }
  }
}