package components.tabListDecorations

import api.Component
import api.addPacketInterceptor
import components.tabListDecorations.packets.client.SetTabListHeaderAndFooter
import packets.client.LoginSuccess

class TabListDecorations : Component() {
  override fun enable() {
    addPacketInterceptor(LoginSuccess) { e ->
      e.after {
        SetTabListHeaderAndFooter(
          header = "{\"text\":\"Hello\"}",
          footer = "{\"text\":\"World\"}"
        ).write(e.user)
      }
    }
  }
}