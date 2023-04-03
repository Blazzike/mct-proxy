package models

import PROTOCOL_VERSION
import api.EventEmitter
import com.google.gson.Gson
import packets.PacketState
import packets.client.Disconnect
import packets.client.LoginSuccess
import packets.server.Handshake
import packets.server.LoginStart
import util.Reader
import util.Writer
import java.net.Socket

class VanillaChannel(private val userChannel: UserChannel) : Channel {
  override var socket: Socket? = null
  override lateinit var reader: Reader
  override lateinit var writer: Writer

  override val onDisconnect = object : EventEmitter<DisconnectEvent>() {}

  override var packetState = PacketState.LOGIN
  override val partner: Channel
    get() = userChannel

  fun init() {
    socket = Socket("localhost", 25566)
    reader = Reader(socket!!.getInputStream())
    writer = Writer(socket!!.getOutputStream())

    val serverAddressParts = arrayOf(
      "localhost",
      "127.0.0.1",
      userChannel.uuid.toString().replace("-", ""), // TODO
      Gson().toJson(userChannel.properties!!),
    )

    Handshake(
      protocolVersion = PROTOCOL_VERSION,
      serverAddress = serverAddressParts.joinToString("\u0000"),
      serverPort = 25566,
      nextState = 2
    ).write(this)

    LoginStart(
      name = userChannel.name,
      hasPlayerUUID = true,
      playerUUID = userChannel.uuid
    ).write(this)

    reader.expectPacket(LoginSuccess)

    packetState = PacketState.PLAY

    try {
      this.runMirror()
    } catch (e: Exception) {
      e.printStackTrace()

      partner.onDisconnect.emit(DisconnectEvent())
      Disconnect(reason = MCText(
        MCText.bold,
        MCText.Color.RED,
        "Uh-oh! ",
        MCText.Color.RESET,
        MCText.undecorated,
        "Something went wrong while connecting to the server.",
      ).toJsonStr()).write(partner)
      partner.socket!!.close()
    }
  }
}