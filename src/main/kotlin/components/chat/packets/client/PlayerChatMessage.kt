package components.chat.packets.client

import packets.BoundTo
import packets.Packet
import packets.PacketInfo
import util.Buffer
import util.Reader
import java.util.*

class PlayerChatMessage(
  var header: Header? = null,
  var body: Body? = null,
  var previousMessages: List<PreviousMessage> = mutableListOf(),
  var unsignedContent: String? = null,
  var filterType: Int? = null,
  var filterTypeBits: BitSet? = null,
  var networkTarget: NetworkTarget? = null,
) : Packet() {
  interface Header {
    val sender: UUID
    val index: Int
    val messageSignature: ByteArray?
  }

  interface Body {
    val message: String
    val timestamp: Long
    val salt: Long
  }

  interface NetworkTarget {
    val chatType: Int
    val networkName: String
    val networkTargetName: String?
  }

  interface PreviousMessage {
    val id: Int
    val signature: ByteArray?
  }

  companion object: PacketInfo<PlayerChatMessage>(0x31, BoundTo.CLIENT)

  override fun read(reader: Reader): Packet {
    val sender = reader.readUUID()
    val index = reader.readVarInt()
    val messageSignaturePresent = reader.readBoolean()
    header = object : Header {
      override val sender = sender
      override val index = index
      override val messageSignature = if (messageSignaturePresent) reader.readByteArray(256) else null

      override fun toString(): String {
        return "Header(sender=${this.sender}, index=${this.index}, messageSignature=${messageSignature?.contentToString()})"
      }
    }

    body = object : Body {
      override val message = reader.readString()
      override val timestamp = reader.readLong()
      override val salt = reader.readLong()

      override fun toString(): String {
        return "Body(message='$message', timestamp=$timestamp, salt=$salt)"
      }
    }

    previousMessages = reader.readArray {
      object : PreviousMessage {
        override val id = reader.readVarInt()
        override val signature = null
//        override val signature = if (messageSignaturePresent) reader.readByteArray(256) else null

//        override fun toString(): String {
//          return "PreviousMessage(id=$id, signature=${signature?.contentToString()})"
//        }
      }
    }

    unsignedContent = if (reader.readBoolean()) reader.readString() else null
    filterType = reader.readVarInt()
    filterTypeBits = if (filterType != 0) reader.readBitSet() else null
    networkTarget = object : NetworkTarget {
      override val chatType = reader.readVarInt()
      override val networkName = reader.readString()
      override val networkTargetName = if (reader.readBoolean()) reader.readString() else null

      override fun toString(): String {
        return "NetworkTarget(chatType=$chatType, networkName='$networkName', networkTargetName=$networkTargetName)"
      }
    }

    return this
  }

  override fun _write(buffer: Buffer) {
    buffer.writeUUID(header!!.sender)
    buffer.writeVarInt(header!!.index)
    buffer.writeBoolean(header!!.messageSignature != null)
    if (header!!.messageSignature != null) {
      buffer.writeBytes(header!!.messageSignature!!)
    }

    buffer.writeString(body!!.message)
    buffer.writeLong(body!!.timestamp)
    buffer.writeLong(body!!.salt)

    buffer.writeArray(previousMessages) {
      buffer.writeVarInt(it.id)
//      buffer.writeBytes(it.signature!!)
    }

    buffer.writeBoolean(unsignedContent != null)
    if (unsignedContent != null) {
      buffer.writeString(unsignedContent!!)
    }

    buffer.writeVarInt(filterType!!)
    if (filterType != 0) {
      buffer.writeBitSet(filterTypeBits!!)
    }

    buffer.writeVarInt(networkTarget!!.chatType)
    buffer.writeString(networkTarget!!.networkName)
    buffer.writeBoolean(networkTarget!!.networkTargetName != null)
    if (networkTarget!!.networkTargetName != null) {
      buffer.writeString(networkTarget!!.networkTargetName!!)
    }
  }

  override fun additionalParams(): Map<String, Any?> {
    return mapOf(
      "header" to header,
      "body" to body,
      "previousMessages" to previousMessages,
      "unsignedContent" to unsignedContent,
      "filterType" to filterType,
      "filterTypeBits" to filterTypeBits,
      "networkTarget" to networkTarget,
    )
  }
}