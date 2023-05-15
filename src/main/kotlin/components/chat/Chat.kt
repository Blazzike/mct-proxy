package components.chat

import api.Component
import api.addPacketInterceptor
import api.users
import components.chat.packets.client.PlayerChatMessage
import components.chat.packets.server.ChatMessage
import components.userOutput.UserOutput
import models.MCText

object Chat : Component() {
  override fun enable() {
    addPacketInterceptor(PlayerChatMessage) { e ->
      this.log("PlayerChatMessage intercepted and cancelled: ${e.packet}")

      e.shouldCancel = true
    }

    addPacketInterceptor(ChatMessage) { e ->
      users.forEach { (_, user) ->
        UserOutput.sendSystemMessage(user, MCText(
          MCText.Color.GREEN,
          e.userChannel.name!!,
          MCText.Color.RESET,
          " ",
          e.packet.message!!
        ))
      }

      e.shouldCancel = true

//      PlayerChatMessage(
//        header = object : PlayerChatMessage.Header {
//          override val sender = e.userChannel.uuid!!
//          override val index = 0
//          override val messageSignature = signatureInts.map { it.toByte() }.toByteArray()
//        } ,
//          body = object : PlayerChatMessage.Body {
//            override val message = "t"
//            override val timestamp = 1682433201372
//            override val salt = 7872237546815127663
//          },
////          previousMessages = listOf(),
//          unsignedContent = null,
//          filterType = 0,
//          filterTypeBits = null,
//          networkTarget = object : PlayerChatMessage.NetworkTarget {
//            override val chatType = 0
//            override val networkName = "{\"insertion\":\"iElmo\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/tell iElmo \"},\"hoverEvent\":{\"action\":\"show_entity\",\"contents\":{\"type\":\"minecraft:player\",\"id\":\"be9482bb-6bcd-4df3-9cf4-9f1fb61c5e93\",\"name\":{\"text\":\"iElmo\"}}},\"text\":\"iElmo\"}"
//            override val networkTargetName = null
//          }
//      ).write(e.userChannel, true)

//      e.shouldCancel = true
    }
  }
}