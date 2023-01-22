package components.chatComponent.packets.server

import packets.Packet
import packets.PacketInfo
import util.Buffer
import util.Reader

class ClientInformation(
  var locale: String? = null,
  var viewDistance: Int? = null,
  var chatMode: Int? = null,
  var chatColors: Boolean? = null,
  var displayedSkinParts: Int? = null,
  var mainHand: Int? = null,
  var enableTextFiltering: Boolean? = null,
  var allowServerListings: Boolean? = null
//  var messageCount: Int? = null, // TODO
//  var acknowledged: BitSet? = null
) : Packet() {
  companion object: PacketInfo<ClientInformation>(0x07)

  override fun read(reader: Reader): Packet {
    locale = reader.readString()
    viewDistance = reader.readByte()
    chatMode = reader.readVarInt()
    chatColors = reader.readBoolean()
    displayedSkinParts = reader.readByte()
    mainHand = reader.readVarInt()
    enableTextFiltering = reader.readBoolean()
    allowServerListings = reader.readBoolean()

    return this
  }

  override fun _write(buffer: Buffer) {
    buffer.writeString(locale!!)
    buffer.writeByte(viewDistance!!)
    buffer.writeVarInt(chatMode!!)
    buffer.writeBoolean(chatColors!!)
    buffer.writeByte(displayedSkinParts!!)
    buffer.writeVarInt(mainHand!!)
    buffer.writeBoolean(enableTextFiltering!!)
    buffer.writeBoolean(allowServerListings!!)
  }

  override fun additionalParams(): Map<String, Any?> {
    return mapOf(
      "locale" to locale,
      "viewDistance" to viewDistance,
      "chatMode" to chatMode,
      "chatColors" to chatColors,
      "displayedSkinParts" to displayedSkinParts,
      "mainHand" to mainHand,
      "enableTextFiltering" to enableTextFiltering,
      "allowServerListings" to allowServerListings
    )
  }
}

