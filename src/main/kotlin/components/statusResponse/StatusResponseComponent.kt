package components.tabListDecorations

import PROTOCOL_VERSION
import VERSION
import api.Component
import api.addPacketInterceptor
import models.MCText
import packets.client.Status
import packets.client.StatusResponse

class StatusResponseComponent : Component() {
  override fun enable() {
    addPacketInterceptor(StatusResponse) { e ->
      e.packet = StatusResponse(
        status = Status(
          version = Status.Version(
            name = VERSION,
            protocol = PROTOCOL_VERSION
          ),
          players = Status.Players(
            max = 20,
            online = 0,
            sample = emptyList()
          ),
          description = MCText().color(MCText.Color.GREEN,
            "Welcome to ",
            MCText().bold("MCTraveler"),
            MCText().newLine,
            MCText().text("Click here to get started.").clickEvent(
              action = MCText.ClickAction.OPEN_URL,
              value = "https://www.google.com",
            ).color(MCText.Color.RED)
          ).toJson(),
          description = MCText(
            MCText.Color.GREEN,
            "Welcome to ",
            MCText.bold,
            "MCTraveler",
            MCText.newLine,
            MCText.clickEvent(
              action = MCText.ClickAction.OPEN_URL,
              value = "https://www.google.com",
            ),
            MCText.Color.RED,
            "Click here to get started."
          ).toJson()
        )
      )

      e.shouldRewrite = true
    }
  }
}