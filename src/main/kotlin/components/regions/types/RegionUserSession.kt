package components.regions.types

import api.Event
import api.EventEmitter
import components.regions.Region
import models.UserChannel

class RegionChangeEvent(
  val exRegion: Region?,
  val region: Region?,
) : Event

class RegionUserSession(val userChannel: UserChannel) {
  val onRegionChange = object : EventEmitter<RegionChangeEvent>() {}

  var currentRegion: Region? = null
    set(value) {
      if (value != field) {
        onRegionChange.emit(RegionChangeEvent(
          region = value,
          exRegion = field,
        ))

        field = value
      }
    }
}

// use entity, use item, block dig, block place // will need to disable TNT and creeper explosions!