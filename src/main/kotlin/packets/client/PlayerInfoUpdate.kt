package packets.client

import packets.Packet
import packets.PacketInfo
import util.Buffer
import util.Reader
import util.Writer
import java.util.*

enum class ActionType {
  ADD_PLAYER,
  INITIALIZE_CHAT,
  UPDATE_GAME_MODE,
  UPDATE_LISTED,
  UPDATE_LATENCY,
  UPDATE_DISPLAY_NAME;
}

open class Action {
  open val type: ActionType
    get() = TODO("Not yet implemented")
  var uuid: UUID? = null

  open fun read(reader: Reader) {
    return TODO("Not yet implemented")
  }

  open fun write(writer: Writer) {
    return TODO("Not yet implemented")
  }
}

class InitializeChat : Action() {
  override val type: ActionType = ActionType.INITIALIZE_CHAT

  var chatSessionID: UUID? = null
  var publicKeyExpiryTime: Long? = null
  var encodedPublicKey: ByteArray? = null
  var publicKeySignature: ByteArray? = null

  override fun read(reader: Reader) {
    if (reader.readBoolean()) {
      chatSessionID = reader.readUUID()
      publicKeyExpiryTime = reader.readLong()
      encodedPublicKey = reader.readByteArray()
      publicKeySignature = reader.readByteArray()
    }
  }

  override fun write(writer: Writer) {
    writer.writeBoolean(chatSessionID != null)
    if (chatSessionID != null) {
      writer.writeUUID(chatSessionID!!)
      writer.writeLong(publicKeyExpiryTime!!)
      writer.writeByteArray(encodedPublicKey!!)
      writer.writeByteArray(publicKeySignature!!)
    }
  }

  override fun toString(): String {
    return "InitializeChat(chatSessionID=$chatSessionID, publicKeyExpiryTime=$publicKeyExpiryTime, encodedPublicKey=${encodedPublicKey?.contentToString()}, publicKeySignature=${publicKeySignature?.contentToString()})"
  }
}

class AddPlayer : Action() {
  override val type: ActionType = ActionType.ADD_PLAYER

  var name: String? = null
  var properties: List<Property> = emptyList()
  val numberOfProperties: Int
    get() = properties.size

  override fun read(reader: Reader) {
    name = reader.readString()
    val numberOfProperties = reader.readVarInt()
    properties = (0 until numberOfProperties!!).map {
      Property.read(reader)
    }
  }

  override fun write(writer: Writer) {
    writer.writeString(name!!)
    writer.writeVarInt(numberOfProperties!!)
    properties!!.forEach { it.write(writer) }
  }

  override fun toString(): String {
    return "AddPlayer(name=$name, numberOfProperties=$numberOfProperties, properties=$properties)"
  }
}

class UpdateGameMode : Action() {
  override val type: ActionType = ActionType.UPDATE_GAME_MODE

  var gameMode: Int = 0

  override fun read(reader: Reader) {
    gameMode = reader.readVarInt()
  }

  override fun write(writer: Writer) {
    writer.writeVarInt(gameMode)
  }

  override fun toString(): String {
    return "UpdateGamemode(gamemode=$gameMode)"
  }
}

class UpdateListed : Action() {
  override val type: ActionType = ActionType.UPDATE_LISTED

  var listed: Boolean = false

  override fun read(reader: Reader) {
    listed = reader.readBoolean()
  }

  override fun write(writer: Writer) {
    writer.writeBoolean(listed)
  }

  override fun toString(): String {
    return "updateListed(listed=$listed)"
  }
}

class UpdateLatency : Action() {
  override val type: ActionType = ActionType.UPDATE_LATENCY

  var ping: Int = 0

  override fun read(reader: Reader) {
    ping = reader.readVarInt()
  }

  override fun write(writer: Writer) {
    writer.writeVarInt(ping)
  }

  override fun toString(): String {
    return "updateLatency(ping=$ping)"
  }
}

class UpdateDisplayName : Action() {
  override val type: ActionType = ActionType.UPDATE_DISPLAY_NAME

  var displayName: String? = null

  override fun read(reader: Reader) {
    displayName = reader.readString()
  }

  override fun write(writer: Writer) {
    writer.writeString(displayName!!)
  }

  override fun toString(): String {
    return "UpdateDisplayName(displayName=$displayName)"
  }
}

class Player {
  var uuid: UUID? = null
  var actions: List<Action>? = null

  fun read(reader: Reader, actionsTypes: EnumSet<ActionType>) {
    uuid = reader.readUUID()
    actions = actionsTypes.map {
      when (it) {
        ActionType.ADD_PLAYER -> AddPlayer()
        ActionType.INITIALIZE_CHAT -> InitializeChat()
        ActionType.UPDATE_GAME_MODE -> UpdateGameMode()
        ActionType.UPDATE_LISTED -> UpdateListed()
        ActionType.UPDATE_LATENCY -> UpdateLatency()
        ActionType.UPDATE_DISPLAY_NAME -> UpdateDisplayName()
      }.apply { read(reader) }
    }
  }

  fun write(writer: Writer) {
    writer.writeUUID(uuid!!)
    actions!!.forEach { it.write(writer) }
  }

  override fun toString(): String {
    return "Player(uuid=$uuid, actions=$actions)"
  }
}

class PlayerInfoUpdate(
  var actions: EnumSet<ActionType>? = null,
  var numberOfActions: Int = 0,
  var playerList: List<Player> = emptyList(),
) : Packet() {
  companion object: PacketInfo<PlayerInfoUpdate>(0x36)

  override fun read(reader: Reader): Packet {
    actions = reader.readEnums(ActionType::class)
    numberOfActions = reader.readVarInt()

    playerList = (0 until numberOfActions).map {
      Player().also { player -> player.read(reader, actions!!) }
    }

    return this
  }

  override fun _write(buffer: Buffer) {
    buffer.writeEnums(ActionType::class, actions!!)
    buffer.writeVarInt(numberOfActions)
    playerList.forEach { it.write(buffer) }
  }

  override fun additionalParams(): Map<String, Any?> {
    return mapOf(
      "actions" to actions,
      "numberOfActions" to numberOfActions,
      "playerList" to playerList
    )
  }
}