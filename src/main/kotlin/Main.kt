
import models.UserChannel
import util.Reader
import util.Writer
import java.net.ServerSocket
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
    val socket = server.accept()
    println("Client connected: ${socket.inetAddress.hostAddress}")

    thread {
      try {
        UserChannel(
          socket = socket,
          reader = Reader(socket.getInputStream()),
          writer = Writer(socket.getOutputStream())
        ).handle()
      } catch (e: Exception) {
        Exception("Client disconnected unexpectedly", e).printStackTrace()
      }
    }
  }
}