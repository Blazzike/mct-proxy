package components.chatComponent

import api.Component
import api.Direction
import api.addPacketInterceptor
import components.chatComponent.packets.server.ChatMessage

class ChatComponent : Component() {
  override fun enable() {
    addPacketInterceptor(Direction.FROM_CLIENT, ChatMessage) { e ->
      println(e.packet)
    }
  }
}