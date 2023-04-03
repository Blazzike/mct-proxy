package components.tabListDecorations

import api.Component
import api.PacketFormation
import api.addPacketInterceptor
import components.tabListDecorations.packets.client.SetTabListHeaderAndFooter
import models.MCText
import packets.client.LoginSuccess

object TabListDecorations : Component() {
  override fun enable() {
    addPacketInterceptor(LoginSuccess, PacketFormation.PROXY) { e ->
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
          ).toJsonStr(),
          footer = MCText(
            MCText.NewLine
          ).toJsonStr()
        ).write(e.userChannel)
      }
    }
  }
}