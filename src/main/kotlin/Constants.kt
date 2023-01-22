import api.Component
import components.tabListDecorations.StatusResponseComponent
import components.tabListDecorations.TabListDecorations
import java.util.*

val COMPONENTS = listOf<Component>(
  TabListDecorations(),
  StatusResponseComponent()
)

const val PROTOCOL_VERSION = 761
const val VERSION = "1.19.3"

object Constants {
  val SERVER_ICON: String = Base64.getEncoder().encodeToString(
    this::class.java.getResource("/server-icon.png")?.readBytes()
  )
}