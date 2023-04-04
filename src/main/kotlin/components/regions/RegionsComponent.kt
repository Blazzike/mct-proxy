package components.regions

import api.Component
import com.google.gson.JsonObject
import components.data.Data
import components.regions.commands.registerCommands
import components.regions.packets.registerInterceptors
import components.regions.types.RegionFlag
import components.regions.types.RegionUserSession
import models.UserChannel
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

    registerInterceptors()
    registerCommands()
  }
}