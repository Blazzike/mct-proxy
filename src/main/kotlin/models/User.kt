package models

import api.Direction
import api.PacketInterceptorManager
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
import java.net.URL
import java.net.URLEncoder
import java.security.MessageDigest
import java.util.*
import javax.crypto.Cipher
import kotlin.concurrent.thread

class User(val writer: Writer, val reader: Reader) {
  var serverClient: ServerClient? = null

  var packetState = PacketState.LOGIN

  var name: String? = null
  var uuid: UUID? = null
  var properties: List<Property>? = null

  fun init() {
    val loginStart = reader.expectPacket(LoginStart)
    name = loginStart.name
    uuid = loginStart.playerUUID

    val verifyToken = ByteArray(4)
    Random().nextBytes(verifyToken)

    EncryptionRequest(
      serverId = "",
      publicKey = keyPair!!.public,
      verifyToken = verifyToken
    ).write(writer)

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
    ).write(writer)

    packetState = PacketState.PLAY

    serverClient = ServerClient(this)
    thread {
      serverClient!!.init()
    }

    this.run()
  }

  private fun run() {
    while (true) {
      val header = reader.readHeader()
//      println("[SERVER] Packet ID: 0x${header.packetId.toString(16)}")

      val packetData = ByteArray(header.remainingLength)
      for (i in 0 until header.remainingLength) {
        packetData[i] = reader.inputStream.read().toByte()
      }

      if (!PacketInterceptorManager.handlePacket(
          Direction.FROM_CLIENT,
          header,
          packetState,
          packetData,
          serverClient!!.writer!!
      )) {
        serverClient!!.writer!!.writeHeader(
          packetId = header.packetId,
          packetLength = header.packetLength
        )

        serverClient!!.writer!!.outputStream.write(packetData)
      }
    }
  }
}