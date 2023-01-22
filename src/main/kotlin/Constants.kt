import api.Component
import components.tabListDecorations.StatusResponseComponent
import components.tabListDecorations.TabListDecorations

val COMPONENTS = listOf<Component>(
  TabListDecorations(),
  StatusResponseComponent()
)

const val PROTOCOL_VERSION = 761
const val VERSION = "1.19.3"