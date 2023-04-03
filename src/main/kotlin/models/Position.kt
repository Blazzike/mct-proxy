package models

class Position(
  val x: Double,
  val y: Double,
  val z: Double,
  val world: String = "world", // TODO
) {
  constructor(
    x: Long,
    y: Long,
    z: Long,
    world: String = "world", // TODO
  ) : this(x.toDouble(), y.toDouble(), z.toDouble(), world)

  override fun toString(): String {
    return "Position(x=$x, y=$y, z=$z)"
  }
}