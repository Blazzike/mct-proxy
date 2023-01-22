package components.tabListDecorations

import api.Component
import api.Direction
import api.addPacketInterceptor
import api.users
import components.tabListDecorations.packets.client.SetTabListHeaderAndFooter
import packets.client.LoginSuccess

class TabListDecorations : Component() {
  override fun enable() {
    addPacketInterceptor(Direction.FROM_SERVER, LoginSuccess) { e ->
      println("yo")
      SetTabListHeaderAndFooter(
        header = "Hello",
        footer = "World"
      ).write(users[e.packet.username]!!)
    }
  }
}