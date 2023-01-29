package components.regions

fun getRegionAt(x: Int, z: Int): Region? {
  return RegionsComponent.regions.firstOrNull { region ->
    x in region.x && z in region.z
  }
}