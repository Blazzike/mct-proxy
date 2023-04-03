package models

import api.EventEmitter
import api.users
import com.google.gson.Gson
import com.google.gson.JsonObject
import keyPair
import packets.PacketState
import packets.client.*
import packets.server.EncryptionResponse
import packets.server.Handshake
import packets.server.LoginStart
import packets.server.StatusRequest
import util.Reader
import util.Writer
import util.uuidFromString
import java.io.EOFException
import java.math.BigInteger
import java.net.HttpURLConnection
import java.net.Socket
import java.net.URL
import java.net.URLEncoder
import java.security.MessageDigest
import java.util.*
import javax.crypto.Cipher
import kotlin.concurrent.thread

class UserChannel(
  override val socket: Socket,
  override val writer: Writer,
  override val reader: Reader
) : Channel {
  var isConnected = false

  private var vanillaChannel: VanillaChannel? = null
  override val partner: Channel
    get() = vanillaChannel!!

  override var packetState = PacketState.HANDSHAKE

  var name: String? = null
  var uuid: UUID? = null
  var properties: List<Property>? = null

  override val onDisconnect = object : EventEmitter<DisconnectEvent>() {}

  fun init() {
    println("${socket.inetAddress.hostAddress} connected")
    isConnected = true
    packetState = PacketState.LOGIN

    val loginStart = reader.expectPacket(LoginStart)
    name = loginStart.name
    uuid = loginStart.playerUUID

    val verifyToken = ByteArray(4)
    Random().nextBytes(verifyToken)

    EncryptionRequest(
      serverId = "",
      publicKey = keyPair!!.public,
      verifyToken = verifyToken
    ).write(this)

    val encryptionResponse = reader.expectPacket(EncryptionResponse)

    val sharedSecret = Cipher.getInstance("RSA").apply {
      init(Cipher.DECRYPT_MODE, keyPair!!.private)
    }.doFinal(encryptionResponse.sharedSecret)

    writer.enableEncryption(sharedSecret)
    reader.enableEncryption(sharedSecret)

    val hash = MessageDigest.getInstance("SHA-1")
    hash.update(sharedSecret)
    hash.update(keyPair!!.public.encoded)

    val hasJoinedConn = URL("https://sessionserver.mojang.com/session/minecraft/hasJoined?username=${
      URLEncoder.encode(name, "UTF-8")
    }&serverId=${
      URLEncoder.encode(BigInteger(hash.digest()).toString(16), "UTF-8")
    }").openConnection() as HttpURLConnection
    hasJoinedConn.connect()

    // parse JSON
    val json = Gson().fromJson(hasJoinedConn.inputStream.bufferedReader().readText(), JsonObject::class.java)

    name = json.get("name").asString
    uuid = uuidFromString(json.get("id").asString)

    properties = json.getAsJsonArray("properties").map {
      val prop = it as JsonObject
      Property(
        name = prop.get("name").asString,
        value = prop.get("value").asString,
        signature = prop.get("signature").asString ?: ""
      )
    }

    LoginSuccess(
      uuid = uuid!!,
      username = name!!,
      properties = properties!!
    ).write(this)

    packetState = PacketState.PLAY

    vanillaChannel = VanillaChannel(this)
    thread {
      vanillaChannel!!.init()
      socket.close()
    }

    users[name!!] = this
    this.runMirror()
    isConnected = false
    if (vanillaChannel!!.socket != null) {
      vanillaChannel!!.socket!!.close()
    }

    users.remove(name!!)
  }

  fun handle() {
    val handshake = reader.expectPacket(Handshake)
    if (handshake.nextState == 1) {
      println("${socket.inetAddress.hostAddress} pinged")

      try {
        this.handleStatus()
      } catch (e: EOFException) {
        e.printStackTrace()
        // TODO add debug
      }
    } else {
      this.init()
    }
  }

  fun handleStatus() {
    packetState = PacketState.STATUS
    reader.expectPacket(StatusRequest)

    StatusResponse(Status(
      version = Status.Version(
        name = "MCTProxy",
        protocol = -1
      ),
    )).write(this)

    val pingRequest = reader.expectPacket(packets.server.PingRequest)
    PingResponse(payload = pingRequest.payload).write(this)

    socket.close()
  }
}