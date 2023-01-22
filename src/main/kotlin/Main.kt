
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

class ClientHandler(private val client: Socket) {
  private val reader = Reader(client.getInputStream())
  private val writer = Writer(client.getOutputStream())

  fun handle() = try {
    val handshake = reader.expectPacket(Handshake)
    if (handshake.nextState == 1) {
      // Client is in status mode
      reader.expectPacket(StatusRequest)

      StatusResponse().write(writer)

      val pingRequest = reader.expectPacket(packets.server.PingRequest)
      packets.client.PingResponse(payload = pingRequest.payload).write(writer)

      client.close()
    } else {
      val user = User(
        reader = reader,
        writer = writer
      )

      user.init()
    }
  } catch (e: Exception) {
    println("Client disconnected: ${e.message}")
    e.printStackTrace()
  }
}