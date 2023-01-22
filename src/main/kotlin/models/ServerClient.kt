package models

import api.Direction
import api.PacketInterceptorManager
import org.json.JSONArray
import packets.PacketState
import packets.client.LoginSuccess
import packets.server.Handshake
import packets.server.LoginStart
import util.Reader
import util.Writer
import java.net.Socket

class ServerClient(private val user: User) {
  var socket: Socket? = null
  var reader: Reader? = null
  var writer: Writer? = null

  var packetState = PacketState.LOGIN

  fun init() {
    socket = Socket("localhost", 25566)
    reader = Reader(socket!!.getInputStream())
    writer = Writer(socket!!.getOutputStream())

    val serverAddressParts = arrayOf(
      "localhost",
      "127.0.0.1",
      user.uuid.toString().replace("-", ""), // TODO
      JSONArray(user.properties!!).toString()
    )

    Handshake(
      protocolVersion = 761,
      serverAddress = serverAddressParts.joinToString("\u0000"),
      serverPort = 25566,
      nextState = 2
    ).write(writer!!)

    LoginStart(
      name = user.name,
      hasPlayerUUID = true,
      playerUUID = user.uuid
    ).write(writer!!)

    reader!!.expectPacket(LoginSuccess)

    packetState = PacketState.PLAY

    this.run()
  }

  private fun run() {
    try {
      while (true) {
        val header = reader!!.readHeader()
//        println("[CLIENT] Packet ID: 0x${header.packetId.toString(16)}")
        val packetData = reader!!.inputStream.readNBytes(header.remainingLength)
        if (!PacketInterceptorManager.handlePacket(Direction.FROM_SERVER, header, packetState, packetData, user.writer)) {
          user.writer.writeHeader(
            packetId = header.packetId,
            packetLength = header.packetLength
          )

          user.writer.outputStream.write(packetData)
        }

      }
    } catch (e: Exception) {
      if (e.message == "Broken pipe") {
        println("Minecraft client disconnected")
      } else {
        e.printStackTrace()
      }

      socket!!.close()
    }
  }
}