package components.commands

import api.Component
import api.addPacketInterceptor
import components.commands.packets.client.*
import components.commands.packets.server.ChatCommand
import components.commands.packets.server.CommandSuggestionsRequest
import components.regions.packets.client.CommandSuggestionsResponse
import components.userOutput.UserOutput
import models.MCText
import models.UserChannel

typealias CommandAction = (e: Commands.ExecuteEvent) -> Commands.CommandResponse

class Command(
  val label: String,
  val action: CommandAction?,
  val arguments: Set<Argument>,
  val aliases: Set<String>
) {
  fun toExecPath(args: List<String>): List<Argument> {
    val result = mutableListOf<Argument>()
    var children = arguments;
    for (arg in args) {
      var argument = children.find { it is LiteralArgument && it.name == arg }
      if (argument == null) {
        argument = children.find { it is DynamicArgument }
      }

      if (argument != null) {
        result.add(argument)
        children = argument.arguments
      } else {
        break
      }
    }

    return result
  }

  fun argumentUsage(argument: Argument): Set<String> {
    val result = mutableSetOf<String>()
    for (arg in argument.arguments) {
      val argUsages = argumentUsage(arg)
      var argLabel = arg.name
      if (arg is DynamicArgument) {
        argLabel = "<$argLabel>"
      }

      if (argUsages.isEmpty()) {
        result.add("${argument.name} $argLabel")
      } else {
        result.addAll(argUsages.map { "${argument.name} $it" })
      }
    }

    if (result.size == 0) {
      var label = argument.name
      if (argument is DynamicArgument) {
        label = "<$label>"
      }

      result.add(label)
    }

    return result
  }

  fun printUsage(label: String, userChannel: UserChannel) {
    val variations = mutableSetOf<String>()
    arguments.forEach { arg ->
      variations.addAll(argumentUsage(arg))
    }

    UserOutput.sendSystemMessage(userChannel, MCText(
      MCText.Color.YELLOW,
      MCText.bold,
      "USAGE",
      MCText.Color.RESET,
      MCText.undecorated,
      "\n",
      variations.joinToString("\n") { "/$label $it" }
    ).toJsonStr())
  }
}

enum class ResponseType(val color: MCText.Color) {
  SUCCESS(MCText.Color.GREEN),
  ERROR(MCText.Color.RED),
  NOTICE(MCText.Color.YELLOW),
}

class CommandInfo(
  val label: String,
)

typealias addNodeType = (node: Node) -> Int

interface Argument {
  val name: String
  val arguments: Set<Argument>
  val action: CommandAction?
  fun toNode(addNode: addNodeType): Node
}

class LiteralArgument(
  override val name: String,
  override val arguments: Set<Argument> = setOf(),
  override val action: CommandAction?
) : Argument {
  override fun toNode(addNode: addNodeType): Node = LiteralNode(
    name = name,
    isExecutable = action != null,
    children = arguments.map {
      addNode(it.toNode(addNode))
    }.toMutableList(),
  )

  override fun toString(): String {
    return "LiteralArgument(name='$name')"
  }
}

fun literalArgument(
  name: String,
  arguments: Set<Argument> = setOf(),
  action: CommandAction? = null,
) = LiteralArgument(
  name = name,
  arguments = arguments,
  action = action,
)

class DynamicArgument(
  override val name: String,
  override val arguments: Set<Argument> = setOf(),
  val suggestionType: SuggestionType? = null,
  val parser: Parser,
  val suggestionProvider: SuggestionProvider? = null,
  override val action: CommandAction?
) : Argument {
  override fun toNode(addNode: addNodeType): Node = ArgumentNode(
    name = name,
    isExecutable = action != null,
    parser = parser,
    suggestionType = suggestionType,
    children = arguments.map {
      addNode(it.toNode(addNode))
    }.toMutableList(),
  )

  override fun toString(): String {
    return "DynamicArgument(name='$name')"
  }
}

typealias SuggestionProvider = (args: List<String>) -> Set<String>

fun dynamicArgument(
  name: String,
  arguments: Set<Argument> = setOf(),
  suggestionType: SuggestionType? = null,
  parser: Parser,
  suggestionProvider: SuggestionProvider? = null,
  action: CommandAction?,
) = DynamicArgument(
  name = name,
  arguments = arguments,
  suggestionType = suggestionType,
  parser = parser,
  suggestionProvider = suggestionProvider,
  action = action,
)

object Commands : Component() {
  val commands: MutableMap<String, Command> = mutableMapOf()

  override fun enable() {
    addPacketInterceptor(CommandsPacket) { e ->
      val addNode = fun(node: Node): Int {
        e.packet.nodes.add(node)

        return e.packet.nodes.size - 1
      }

      commands.values.map { command ->
        val parentNodeIndex = addNode(
          LiteralNode(
            name = command.label,
            isExecutable = command.action != null,
            children = command.arguments.map {
              addNode(it.toNode(addNode))
            }.toMutableList(),
          )
        )

        e.packet.nodes[e.packet.rootIndex!!].children.add(parentNodeIndex)

        command.aliases.forEach {
          e.packet.nodes[e.packet.rootIndex!!].children.add(
            addNode(
              LiteralNode(
                name = it,
                isExecutable = command.action != null,
                redirectNode = parentNodeIndex,
              )
            )
          )
        }
      }

      e.shouldRewrite = true
    }

    addPacketInterceptor(ChatCommand) { e ->
      var args = e.packet.command!!.split(' ')
      val label = args[0]
      args = args.subList(1, args.size)

      commands[label]?.let {
        e.shouldCancel = true
        val arguments = it.toExecPath(args)
        val executeEvent = ExecuteEvent(e.userChannel, args)
        for (argument in arguments.reversed()) {
          if (argument.action != null) {
            argument.action!!(executeEvent).send(e.userChannel)

            return@let
          }
        }

        if (it.action != null) {
          it.action!!(executeEvent).send(e.userChannel)

          return@let
        }

        it.printUsage(label, e.userChannel)
      }
    }

    addPacketInterceptor(CommandSuggestionsRequest) { e ->
      var args = e.packet.text!!.substring(1).split(' ')
      val label = args[0]
      args = args.subList(1, args.size)

      commands[label]?.let {
        e.shouldCancel = true

        val arguments = it.toExecPath(args)
        val lastArg = arguments.lastOrNull()
        if (lastArg != null && lastArg is DynamicArgument && lastArg.suggestionProvider != null) {
          val suggestions = lastArg.suggestionProvider!!(args)
          if (suggestions.isEmpty()) {
            return@let
          }

          CommandSuggestionsResponse(
            id = e.packet.transactionId!!,
            start = "/$label ${args.slice(0 until args.lastIndex).joinToString(" ")}".length + 1,
            length = args.last().length,
            suggestions = suggestions.map {
              CommandSuggestionsResponse.Match(
                match = it,
                tooltip = null,
              )
            }.toMutableList(),
          ).write(e.userChannel)
        }
      }
    }
  }

  class CommandResponse(val responseType: ResponseType, val message: MCText) {
    constructor(responseType: ResponseType, message: String) : this(responseType, MCText(message))

    fun send(userChannel: UserChannel) {
      val json = MCText(
        responseType.color,
        MCText.bold,
        responseType.name,
        MCText.undecorated,
        MCText.Color.RESET,
        " ",
        message
      ).toJsonStr()

      UserOutput.sendSystemMessage(userChannel, json)
    }
  }

  class ExecuteEvent(
    val userChannel: UserChannel,
    val args: List<String>,
  ) {
    fun sendSystemMessage(message: String, isActionBar: Boolean = false) {
      UserOutput.sendSystemMessage(userChannel, message, isActionBar)
    }
  }

  fun register(
    label: String,
    aliases: Set<String> = setOf(),
    arguments: Set<Argument> = setOf(),
    action: CommandAction? = null,
  ) {
    val command = Command(
      label = label,
      aliases = aliases,
      arguments = arguments,
      action = action,
    )

    commands[label] = command
    aliases.forEach { alias ->
      commands[alias] = command
    }
  }
}