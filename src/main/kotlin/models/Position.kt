package models

class Position(
  val x: Long,
  val y: Long,
  val z: Long,
) {
  override fun toString(): String {
    return "Position(x=$x, y=$y, z=$z)"
  }
}