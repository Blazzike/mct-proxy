import api.Component
import components.commands.Commands
import components.inventoryTracker.InventoryTracker
import components.noRain.NoRain
import components.regions.RegionsComponent
import components.tabListDecorations.StatusResponseComponent
import components.tabListDecorations.TabListDecorations
import components.userOutput.UserOutput
import java.util.*
import kotlin.reflect.KClass

val COMPONENTS = listOf(
  UserOutput,
  Commands,
  TabListDecorations,
  StatusResponseComponent,
  NoRain,
  InventoryTracker,
  RegionsComponent
)

fun <T : Component> getComponent(clazz: KClass<out T>): T {
  return COMPONENTS.stream()
    .filter { it::class == clazz }
    .findFirst().orElseThrow() as T
}

const val PROTOCOL_VERSION = 761
const val VERSION = "1.19.3"

object Constants {
  val SERVER_ICON: String = Base64.getEncoder().encodeToString(
    this::class.java.getResource("/server-icon.png")?.readBytes()
  )
}