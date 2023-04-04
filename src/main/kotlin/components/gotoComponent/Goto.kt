package components.gotoComponent

import SERVERS
import api.Component
import components.commands.Commands
import components.commands.ResponseType
import components.commands.literalArgument

object Goto : Component() {
  override fun enable() {
    Commands.register(
      label = "goto",
      arguments = setOf(
        literalArgument("old")  { e ->
          try {
            e.userChannel.goto(SERVERS[0])
          } catch (_: IllegalArgumentException) {
            return@literalArgument Commands.CommandResponse(ResponseType.ERROR, "You are already on the old world")
          }

          return@literalArgument Commands.CommandResponse(ResponseType.SUCCESS, "Goto old")
        },
        literalArgument("new")  { e ->
          try {
            e.userChannel.goto(SERVERS[1])
          } catch (_: IllegalArgumentException) {
            return@literalArgument Commands.CommandResponse(ResponseType.ERROR, "You are already on the new world")
          }

          return@literalArgument Commands.CommandResponse(ResponseType.SUCCESS, "Goto new")
        },
      )
    )
  }
}