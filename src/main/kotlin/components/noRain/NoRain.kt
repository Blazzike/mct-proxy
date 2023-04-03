package components.noRain

import api.Component
import components.commands.Commands
import components.commands.Commands.CommandResponse
import components.commands.ResponseType
import components.noRain.packets.client.GameEvent

object NoRain : Component() {
  override fun enable() {
    Commands.register("norain") { e ->
      GameEvent(
        event = GameEvent.Type.END_RAINING,
        value = 0f
      ).write(e.userChannel)

      return@register CommandResponse(ResponseType.SUCCESS, "Rain has been hidden")
    }
  }
}