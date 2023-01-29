package components.userOutput

import api.Component
import components.userOutput.packets.client.SystemChatMessage
import models.MCText
import models.UserChannel

object UserOutput : Component() {
  fun sendSystemMessage(userChannel: UserChannel, message: String, isActionBar: Boolean = false) {
    SystemChatMessage(message, isActionBar).write(userChannel)
  }

  fun sendSystemMessage(userChannel: UserChannel, message: MCText, isActionBar: Boolean = false) {
    sendSystemMessage(userChannel, message.toJsonStr(), isActionBar)
  }
}