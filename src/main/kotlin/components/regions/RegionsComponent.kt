package components.regions

import api.Component
import api.addPacketInterceptor
import components.regions.packets.server.*
import components.regions.types.RegionUserSession
import components.regions.types.removeRegionScoreboardDisplay
import components.regions.types.sendRegionScoreboardDisplay
import components.userOutput.UserOutput
import models.MCText
import models.UserChannel
import packets.client.LoginSuccess

class Region(
  val id: Int,
  val name: String,
  val x: IntRange,
  val z: IntRange,
)

object RegionsComponent : Component() {
  val regions = mutableListOf<Region>(Region(
    id = 0,
    name = "iElmo's Place",
    x = -50..50,
    z = -50..50,
  ))

  val userSessions = hashMapOf<UserChannel, RegionUserSession>()

  override fun enable() {
    addPacketInterceptor(LoginSuccess) { e ->
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

        if (isEntering) {
          sendRegionScoreboardDisplay(e.userChannel, r.region!!)
        } else {
          removeRegionScoreboardDisplay(e.userChannel)
        }
      }
    }

    addPacketInterceptor(PositionPacket) { e ->
      userSessions[e.userChannel]!!.currentRegion = getRegionAt(e.packet.x!!.toInt(), e.packet.z!!.toInt())
    }

    addPacketInterceptor(SynchronizePlayerPosition) { e ->
      if (e.packet.isXRelative || e.packet.isZRelative) {
        return@addPacketInterceptor
      }

      userSessions[e.userChannel]!!.currentRegion = getRegionAt(e.packet.x!!.toInt(), e.packet.z!!.toInt())
    }

    addPacketInterceptor(UseItemOn) { e ->
      val pos = e.packet.position!!
      val region = getRegionAt(pos.x.toInt(), pos.z.toInt())

      if (region != null) {
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

        e.shouldCancel = true
      }
    }

    addPacketInterceptor(PlayerAction) { e ->
      if (!e.packet.status!!.isDig) {
        return@addPacketInterceptor
      }

      val pos = e.packet.blockPosition!!
      val region = getRegionAt(pos.x.toInt(), pos.z.toInt())

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
//      println(e.packet)
//      e.shouldCancel = true
    }
  }
}