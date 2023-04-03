package components.regions

import api.Component
import api.PacketFormation
import api.addPacketInterceptor
import com.google.gson.JsonObject
import components.data.Data
import components.inventoryTracker.InventoryTracker
import components.inventoryTracker.models.ItemStack
import components.regions.info.SafeUseItems
import components.regions.packets.server.*
import components.regions.types.RegionFlag
import components.regions.types.RegionUserSession
import components.regions.types.removeRegionScoreboardDisplay
import components.regions.types.sendRegionScoreboardDisplay
import components.userOutput.UserOutput
import models.MCText
import models.Position
import models.UserChannel
import packets.client.LoginSuccess
import java.util.*

class Region(
  val id: String,
  val name: String,
  val x: IntRange,
  val y: IntRange,
  val z: IntRange,
  val world: String,
  val children: MutableSet<Region> = mutableSetOf(),
  val members: MutableSet<UUID> = mutableSetOf(),
  val flags: MutableSet<RegionFlag> = mutableSetOf(),
) {
  override fun toString(): String {
    return "Region(id='$id', name='$name', x=$x, y=$y, z=$z, world='$world', children=$children, members=$members, flags=$flags)"
  }
}

object RegionsComponent : Component() {
  val regions = mutableSetOf<Region>()

  val userSessions = hashMapOf<UserChannel, RegionUserSession>()

  override fun enable() {
    // Load regions from regions.json
    log("Loading regions...")
    Data.readJsonObjectFile("regions.json").let { json ->
      regions.addAll(json.asMap().map { (key, value) ->
        deserializeRegion(key, value as JsonObject)
      })
    }

    addPacketInterceptor(LoginSuccess, PacketFormation.PROXY) { e ->
      val userSession = RegionUserSession(e.userChannel)
      userSessions[e.userChannel] = userSession

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
        userSessions.remove(e.userChannel)
      }
    }

    addPacketInterceptor(PositionPacket) { e ->
      userSessions[e.userChannel]!!.currentRegion = getRegionAt(Position(
        x = e.packet.x!!,
        y = e.packet.feetY!!,
        z = e.packet.z!!,
      ))
    }

    addPacketInterceptor(SynchronizePlayerPosition) { e ->
      if (e.packet.isXRelative || e.packet.isZRelative) {
        return@addPacketInterceptor
      }

      userSessions[e.userChannel]!!.currentRegion = getRegionAt(Position(
        x = e.packet.x!!,
        y = e.packet.y!!,
        z = e.packet.z!!,
      ))
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
      val region = userSessions[e.userChannel]!!.currentRegion
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
}