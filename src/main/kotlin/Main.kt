
import models.User
import packets.client.StatusResponse
import packets.server.Handshake
import packets.server.StatusRequest
import util.Reader
import util.Writer
import java.net.ServerSocket
import java.net.Socket
import java.security.KeyPair
import java.security.KeyPairGenerator
import kotlin.concurrent.thread

var keyPair: KeyPair? = null

fun main() {
  KeyPairGenerator.getInstance("RSA").apply {
    initialize(1024)
    keyPair = generateKeyPair()
  }

  COMPONENTS.forEach {
    println("Enabling component ${it.javaClass.simpleName}")
    it.enable()
  }

  val server = ServerSocket(25565)
  println("Server started on port ${server.localPort}")

  while (true) {
    val client = server.accept()
    println("Client connected: ${client.inetAddress.hostAddress}")

    thread {
      ClientHandler(client).handle()
    }
  }
}

class ClientHandler(private val socket: Socket) {
  private val reader = Reader(socket.getInputStream())
  private val writer = Writer(socket.getOutputStream())

  fun handle() = try {
    val handshake = reader.expectPacket(Handshake)
    val user = User(
      socket = socket,
      reader = reader,
      writer = writer
    )

    if (handshake.nextState == 1) {
      // Client is in status mode
      reader.expectPacket(StatusRequest)

      StatusResponse().write(user)

      val pingRequest = reader.expectPacket(packets.server.PingRequest)
      packets.client.PingResponse(payload = pingRequest.payload).write(user)

      socket.close()
    } else {
      user.init()
    }
  } catch (e: Exception) {
    println("Client disconnected: ${e.message}")
    e.printStackTrace()
  }
}