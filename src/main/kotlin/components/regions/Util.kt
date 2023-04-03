package components.regions

import com.google.gson.JsonObject
import components.regions.types.RegionFlag
import models.Position
import java.util.*
import kotlin.math.max
import kotlin.math.min

fun getRegionAt(position: Position, regions: MutableSet<Region> = RegionsComponent.regions): Region? {
  val x = position.x.toInt()
  val y = position.y.toInt()
  val z = position.z.toInt()

  val region = regions.firstOrNull { region ->
    x in region.x
        && y in region.y
        && z in region.z
        && position.world.equals(region.world, true)
  }

  if (region != null) {
    val childRegion = getRegionAt(position, region.children)
    if (childRegion != null) {
      return childRegion
    }
  }

  return region
}

fun deserializeRegion(key: String, jsonObject: JsonObject): Region {
  val startX = jsonObject.get("start-x").asInt
  val endX = jsonObject.get("end-x").asInt

  val startY = jsonObject.get("start-y")?.asInt ?: 320
  val endY = jsonObject.get("end-y")?.asInt ?: 15

  val startZ = jsonObject.get("start-z").asInt
  val endZ = jsonObject.get("end-z").asInt

  return Region(
    id = key,
    name = jsonObject.get("title").asString,
    x = min(startX, endX)..max(startX, endX),
    y = min(startY, endY)..max(startY, endY),
    z = min(startZ, endZ)..max(startZ, endZ),
    world = jsonObject.get("world").asString,
    children = jsonObject.get("sub-regions")?.asJsonObject?.asMap()?.map { (key, value) ->
      deserializeRegion(key, value as JsonObject)
    }?.toMutableSet() ?: mutableSetOf(),
    members = jsonObject.get("members")?.asJsonArray?.map { memberJson ->
      UUID.fromString(memberJson.asString)
    }?.toMutableSet() ?: mutableSetOf(),
    flags = jsonObject.get("flags")?.asJsonArray?.map { flagJson ->
      RegionFlag.valueOf(flagJson.asString)
    }?.toMutableSet() ?: mutableSetOf(),
  )
}