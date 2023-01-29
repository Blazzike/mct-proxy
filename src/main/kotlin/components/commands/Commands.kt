package components.commands

import api.Component
import api.addPacketInterceptor
import components.commands.packets.client.CommandsPacket
import components.commands.packets.client.LiteralNode
import components.commands.packets.server.ChatCommand
import components.userOutput.UserOutput
import models.MCText
import models.UserChannel

typealias CommandAction = (e: Commands.ExecuteEvent) -> Commands.CommandResponse

class Command(val label: String, val action: CommandAction)

object Commands : Component() {
  val commands: MutableList<Command> = mutableListOf()

  override fun enable() {
    addPacketInterceptor(CommandsPacket) { e ->
      commands.forEach { command ->
        e.packet.nodes.add(
          LiteralNode(
            name = command.label,
            isExecutable = true,
            children = mutableListOf(),
          )
        )

        e.packet.nodes[e.packet.rootIndex!!].children.add(e.packet.nodes.size - 1)
      }

      e.shouldRewrite = true
    }

    addPacketInterceptor(ChatCommand) { e ->
      var args = e.packet.command!!.split(' ')
      val label = args[0]
      args = args.subList(1, args.size)

      commands.forEach { command ->
        if (command.label == label) {
          command.action(ExecuteEvent(e.userChannel, args)).send(e.userChannel)

          e.shouldCancel = true

          return@forEach
        }
      }
    }
  }

  class CommandResponse(val type: Type, val message: MCText) {
    constructor(type: Type, message: String) : this(type, MCText(message))

    enum class Type(val color: MCText.Color) {
      SUCCESS(MCText.Color.GREEN),
      ERROR(MCText.Color.RED),
      NOTICE(MCText.Color.YELLOW),
    }

    fun send(userChannel: UserChannel) {
      val json = MCText(
        type.color,
        MCText.bold,
        type.name,
        MCText.undecorated,
        MCText.Color.WHITE,
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

  fun register(label: String, action: CommandAction) {
    commands.add(Command(label, action))
  }
}