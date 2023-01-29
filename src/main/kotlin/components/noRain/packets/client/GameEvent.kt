package components.noRain.packets.client

import packets.BoundTo
import packets.Packet
import packets.PacketInfo
import util.Buffer

class GameEvent(
  val event: Type,
  val value: Float
) : Packet() {
  enum class Type {
    NO_RESPAWN_BLOCK_AVAILABLE,
    END_RAINING,
    BEGIN_RAINING,
    CHANGE_GAMEMODE,
    WIN_GAME,
    DEMO_EVENT,
    ARROW_HIT_PLAYER,
    RAIN_LEVEL_CHANGE,
    THUNDER_LEVEL_CHANGE,
    PLAY_PUFFER_FISH_STING_SOUND,
    PLAY_ELDER_GUARDIAN_MOB_APPEARANCE,
    ENABLE_RESPAWN_SCREEN,
  }

  companion object: PacketInfo<GameEvent>(0x1C, BoundTo.CLIENT)

  override fun _write(buffer: Buffer) {
    buffer.writeByte(event.ordinal)
    buffer.writeFloat(value)
  }
}