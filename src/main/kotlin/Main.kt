
import models.UserChannel
import util.Reader
import util.Writer
import java.net.ServerSocket
import java.security.KeyPair
import java.security.KeyPairGenerator
import kotlin.concurrent.thread

var keyPair: KeyPair? = null

/**
 * TODO
 *
 * - afk
 * - back
 * - invsee
 * - kick
 * - ban
 * - list/online
 * - lookup
 * - msg/pm/r/whisper
 * - mute
 * - shrug
 */

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

    thread {
      UserChannel(
        socket = socket,
        reader = Reader(socket.getInputStream()),
        writer = Writer(socket.getOutputStream())
      ).handle()
    }
  }
}