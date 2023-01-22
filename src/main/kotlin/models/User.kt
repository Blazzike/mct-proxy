package models

import api.users
import keyPair
import org.json.JSONObject
import packets.PacketState
import packets.client.EncryptionRequest
import packets.client.LoginSuccess
import packets.client.Property
import packets.server.EncryptionResponse
import packets.server.LoginStart
import util.Reader
import util.Writer
import util.uuidFromString
import java.math.BigInteger
import java.net.HttpURLConnection
import java.net.Socket
import java.net.URL
import java.net.URLEncoder
import java.security.MessageDigest
import java.util.*
import javax.crypto.Cipher
import kotlin.concurrent.thread

class User(
  override val socket: Socket,
  override val writer: Writer,
  override val reader: Reader
) : Channel {
  var isConnected = false

  private var serverClient: ServerClient? = null
  override val partner: Channel
    get() = serverClient!!

  override var packetState = PacketState.HANDSHAKE

  var name: String? = null
  var uuid: UUID? = null
  var properties: List<Property>? = null

  fun init() {
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
    val json = JSONObject(hasJoinedConn.inputStream.bufferedReader().readText())

    name = json.getString("name")
    uuid = uuidFromString(json.getString("id"))

    properties = json.getJSONArray("properties").map {
      val prop = it as JSONObject
      Property(
        name = prop.getString("name"),
        value = prop.getString("value"),
        signature = prop.optString("signature")
      )
    }

    LoginSuccess(
      uuid = uuid!!,
      username = name!!,
      properties = properties!!
    ).write(this)

    packetState = PacketState.PLAY

    serverClient = ServerClient(this)
    thread {
      serverClient!!.init()
      socket.close()
    }

    users[name!!] = this
    this.runMirror()
    isConnected = false
    serverClient!!.socket.close()
    users.remove(name!!)
  }
}