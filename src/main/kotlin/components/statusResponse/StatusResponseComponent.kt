package components.tabListDecorations

import Constants.SERVER_ICON
import PROTOCOL_VERSION
import VERSION
import api.Component
import api.addPacketInterceptor
import api.users
import models.MCText
import packets.client.Status
import packets.client.StatusResponse

object StatusResponseComponent : Component() {
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
            online = users.values.size,
            sample = users.values.map { Status.Player(it.name!!, it.uuid!!.toString() ) }
          ),
          description = MCText(
            MCText.Color.GREEN,
            "Welcome to ",
            MCText.bold,
            "MCTraveler",
            MCText.newLine,
            MCText.undecorated,
            MCText.Color.GRAY,
            "Now in rewrite ",
            MCText.Color.DARK_AQUA,
            "Beta",
          ).toCodedString(),
          favicon = "data:image/png;base64,$SERVER_ICON"
        )
      )

      e.shouldRewrite = true
    }
  }
}