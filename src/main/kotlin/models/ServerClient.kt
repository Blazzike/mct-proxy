package models

import org.json.JSONArray
import packets.PacketState
import packets.client.LoginSuccess
import packets.server.Handshake
import packets.server.LoginStart
import util.Reader
import util.Writer
import java.net.Socket

class ServerClient(private val user: User) : Channel {
  override lateinit var socket: Socket
  override lateinit var reader: Reader
  override lateinit var writer: Writer

  override var packetState = PacketState.LOGIN
  override val partner: Channel
    get() = user

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
    ).write(this)

    LoginStart(
      name = user.name,
      hasPlayerUUID = true,
      playerUUID = user.uuid
    ).write(this)

    reader.expectPacket(LoginSuccess)

    packetState = PacketState.PLAY

    this.runMirror()
  }
}