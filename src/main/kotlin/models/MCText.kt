package models

class MCText {
  enum class Color {
    BLACK("black"),
    DARK_BLUE("dark_blue"),
    DARK_GREEN("dark_green"),
    DARK_AQUA("dark_aqua"),
    DARK_RED("dark_red"),
    DARK_PURPLE("dark_purple"),
    GOLD("gold"),
    GRAY("gray"),
    DARK_GRAY("dark_gray"),
    BLUE("blue"),
    GREEN("green"),
    AQUA("aqua"),
    RED("red"),
    LIGHT_PURPLE("light_purple"),
    YELLOW("yellow"),
    WHITE("white");

    private val value: String
    constructor(value: String) {
      this.value = value
    }
  }

  enum class ClickAction {
    OPEN_URL("open_url"),
    OPEN_FILE("open_file"),
    RUN_COMMAND("run_command"),
    SUGGEST_COMMAND("suggest_command"),
    CHANGE_PAGE("change_page"),
    COPY_TO_CLIPBOARD("copy_to_clipboard");

    private val value: String
    constructor(value: String) {
      this.value = value
    }
  }

  private val parts = mutableListOf<MCText>()
  private var color: Color? = null
  private var text: String = ""
  private var isBold: Boolean = false
  private var clickAction: ClickAction? = null
  private var clickValue: String? = null

  private fun addParts(vararg parts: Any) {
    parts.map {
      if (it is MCText) {
        this.parts.add(it)
      } else {
        this.parts.add(MCText().text(it.toString()))
      }
    }
  }

  fun color(color: Color, vararg parts: Any): MCText {
    this.color = color
    this.addParts(*parts)

    return this
  }

  fun bold(vararg parts: Any): MCText {
    this.isBold = true
    this.addParts(*parts)

    return this
  }

  fun text(text: String, vararg parts: Any): MCText {
    this.text = text
    this.addParts(*parts)

    return this
  }

  fun clickEvent(action: ClickAction, value: String, vararg parts: Any): MCText {
    this.clickAction = action
    this.clickValue = value
    this.addParts(*parts)

    return this
  }

  val newLine: MCText
    get() = MCText().text("\n")

  fun toJson(): String {
    println(this)

    return ""
  }

  override fun toString(): String {
    return "MCText(parts=${parts.joinToString("\n")}, color=$color, text='$text', isBold=$isBold, clickAction=$clickAction, clickValue=$clickValue)"
  }
}