package models

import api.PacketInterceptorManager
import packets.PacketState
import util.Reader
import util.Writer
import java.net.Socket
import java.net.SocketException

interface Channel {
  val socket: Socket?
  val writer: Writer
  val reader: Reader
  val packetState: PacketState
  val partner: Channel
}

fun Channel.runMirror() {
  while (true) {
    try {
      val header = reader.readHeader()
//      if (this is VanillaChannel) {
//        when (header.packetId) {
//          0x27 -> null
//          0x50 -> null
//          0x28 -> null
//          0x3e -> null
//          0x20 -> null
//          0x5a -> null
//          0x5e -> null
//          0x09 -> null
//          0x64 -> null
//          0x23 -> null
//          0x19 -> null
//          0x66 -> null
//          0x4e -> null
//          0x3a -> null
//          0x3f -> null
//          0x1f -> null
//          0x29-> null
//          0x00-> null
//          0x36-> null
//          else -> println("Server: $header")
//        }
//      }

      PacketInterceptorManager.handlePacket(this, header)
    } catch (e: SocketException) {
      println("Socket closed")
      break
    } catch (e: EOFException) {
      println("EOF")
      e.printStackTrace()
      break
    }
  }
}