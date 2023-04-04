package components.regions.packets

import api.PacketFormation
import api.addPacketInterceptor
import components.inventoryTracker.InventoryTracker
import components.inventoryTracker.models.ItemStack
import components.regions.RegionsComponent
import components.regions.getRegionAt
import components.regions.info.SafeUseItems
import components.regions.packets.server.*
import components.regions.types.RegionUserSession
import components.regions.types.removeRegionScoreboardDisplay
import components.regions.types.sendRegionScoreboardDisplay
import components.userOutput.UserOutput
import models.MCText
import models.Position
import packets.client.LoginSuccess

fun registerInterceptors() {
  addPacketInterceptor(LoginSuccess, PacketFormation.PROXY) { e ->
    val userSession = RegionUserSession(e.userChannel)
    RegionsComponent.userSessions[e.userChannel] = userSession

    userSession.onRegionChange.listen { r ->
      val isEntering = r.region != null

      UserOutput.sendSystemMessage(
        e.userChannel,
        MCText(
          MCText.Color.GRAY,
          if (isEntering) "Entering" else "Leaving",
          " ",
          MCText.Color.DARK_PURPLE,
          (r.region ?: r.exRegion)!!.name,
        ),
        true
      )

      removeRegionScoreboardDisplay(e.userChannel)
      if (isEntering) {
        sendRegionScoreboardDisplay(e.userChannel, r.region!!)
      }
    }

    e.userChannel.onDisconnect.listen {
      RegionsComponent.userSessions.remove(e.userChannel)
    }
  }

  addPacketInterceptor(PositionPacket) { e ->
    RegionsComponent.userSessions[e.userChannel]!!.currentRegion = getRegionAt(
      Position(
      x = e.packet.x!!,
      y = e.packet.feetY!!,
      z = e.packet.z!!,
    )
    )
  }

  addPacketInterceptor(SynchronizePlayerPosition) { e ->
    if (e.packet.isXRelative || e.packet.isZRelative) {
      return@addPacketInterceptor
    }

    RegionsComponent.userSessions[e.userChannel]!!.currentRegion = getRegionAt(
      Position(
      x = e.packet.x!!,
      y = e.packet.y!!,
      z = e.packet.z!!,
    )
    )
  }

  addPacketInterceptor(UseItemOn) { e ->
    val pos = e.packet.position!!
    val region = getRegionAt(pos)

    if (region != null) {
      UserOutput.sendSystemMessage(
        e.userChannel,
        MCText(
          MCText.Color.GRAY,
          "You cannot interact with things in ",
          MCText.Color.DARK_PURPLE,
          region.name,
        ),
        true
      )

      e.shouldCancel = true
    }
  }

  addPacketInterceptor(PlayerAction) { e ->
    if (!e.packet.action!!.isDig) {
      return@addPacketInterceptor
    }

    val pos = e.packet.blockPosition!!
    val region = getRegionAt(pos)

    if (region != null) {
      UserOutput.sendSystemMessage(
        e.userChannel,
        MCText(
          MCText.Color.GRAY,
          "You cannot dig blocks in ",
          MCText.Color.DARK_PURPLE,
          region.name,
        ),
        true
      )

      e.shouldCancel = true
    }
  }

  addPacketInterceptor(UseItem) { e ->
    // TODO check whether looking into a region rather than just the player's position
    val region = RegionsComponent.userSessions[e.userChannel]!!.currentRegion
    if (region == null) {
      return@addPacketInterceptor
    }

    val inventory = InventoryTracker.getInventory(e.userChannel)!!
    val item: ItemStack = when (e.packet.hand!!) {
      UseItem.Hand.MAIN -> inventory.heldItem
      UseItem.Hand.OFF -> inventory.offhandItem
    } ?: return@addPacketInterceptor

    if (SafeUseItems[item.itemId] != null) {
      return@addPacketInterceptor
    }

    e.shouldCancel = true
    UserOutput.sendSystemMessage(
      e.userChannel,
      MCText(
        MCText.Color.GRAY,
        "You cannot use items in ",
        MCText.Color.DARK_PURPLE,
        region.name,
      ),
      true
    )
  }
}