package components.chatComponent

import api.Component
import api.addPacketInterceptor
import components.chatComponent.packets.server.ChatMessage

class ChatComponent : Component() {
  override fun enable() {
    addPacketInterceptor(ChatMessage) { e ->
      println(e.packet)
    }
  }
}