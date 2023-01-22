package components.tabListDecorations

import api.Component
import api.addPacketInterceptor
import components.tabListDecorations.packets.client.SetTabListHeaderAndFooter
import models.MCText
import packets.client.LoginSuccess

class TabListDecorations : Component() {
  override fun enable() {
    addPacketInterceptor(LoginSuccess) { e ->
      e.after {
        SetTabListHeaderAndFooter(
          header = MCText(
            " ".repeat(40),
            MCText.NewLine,
            MCText.Color.GREEN,
            "play.MCTraveler.eu",
            MCText.NewLine,
            MCText.Color.GRAY,
            "In Beta",
            MCText.NewLine,
            " "
          ).toJson(),
          footer = MCText(
            MCText.NewLine,
            MCText.NewLine
          ).toJson()
        ).write(e.user)
      }
    }
  }
}