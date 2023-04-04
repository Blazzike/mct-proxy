package components.regions.commands

import api.users
import components.commands.Commands
import components.commands.ResponseType
import components.commands.dynamicArgument
import components.commands.literalArgument
import components.commands.packets.client.ParserString
import components.commands.packets.client.SuggestionType
import models.Position
import models.UserChannel
import java.util.*

fun registerCommands() {
  val startPositions = WeakHashMap<UserChannel, Position>()

  Commands.register(
    label = "region",
    aliases = setOf("rg", "reg"),
    arguments = setOf(
      literalArgument(
        name = "start",
      ) { e ->
//        startPositions[e.userChannel] = e.userChannel.user!!.position

        return@literalArgument Commands.CommandResponse(
          ResponseType.SUCCESS,
          "Start region",
        )
      },
      literalArgument(
        name = "end",
      ) { e ->
        return@literalArgument Commands.CommandResponse(
          ResponseType.SUCCESS,
          "End region",
        )
      },
      literalArgument(
        name = "add",
        arguments = setOf(
          dynamicArgument(
            name = "username",
            parser = ParserString(ParserString.ParserStringType.SINGLE_WORD),
            suggestionType = SuggestionType.ASK_SERVER,
            suggestionProvider = { args ->
              users.values.map { it.name!! }.toSet()
            },
          ) { e ->
            return@dynamicArgument Commands.CommandResponse(
              ResponseType.SUCCESS,
              "Add member",
            )
          }
        ),
      ),
      literalArgument(
        name = "remove",
        arguments = setOf(
          dynamicArgument(
            name = "username",
            parser = ParserString(ParserString.ParserStringType.SINGLE_WORD),
            suggestionType = SuggestionType.ASK_SERVER,
            suggestionProvider = { args ->
              users.values.map { it.name!! }.toSet()
            },
          ) { e ->
            return@dynamicArgument Commands.CommandResponse(
              ResponseType.SUCCESS,
              "Remove member",
            )
          }
        ),
      ),
      literalArgument(
        name = "delete",
      ) { e ->
        return@literalArgument Commands.CommandResponse(
          ResponseType.SUCCESS,
          "Delete region",
        )
      },
    ),
  )
}