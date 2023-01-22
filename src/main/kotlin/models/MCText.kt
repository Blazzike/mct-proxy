package models

import org.json.JSONArray
import org.json.JSONObject

interface MCTextComponent

class MCText(private vararg val parts: Any) {
  enum class Color(
    val value: String,
    val code: String,
  ) : MCTextComponent {
    BLACK("black", "0"),
    DARK_BLUE("dark_blue", "1"),
    DARK_GREEN("dark_green", "2"),
    DARK_AQUA("dark_aqua", "3"),
    DARK_RED("dark_red", "4"),
    DARK_PURPLE("dark_purple", "5"),
    GOLD("gold", "6"),
    GRAY("gray", "7"),
    DARK_GRAY("dark_gray", "8"),
    BLUE("blue", "9"),
    GREEN("green", "a"),
    AQUA("aqua", "b"),
    RED("red", "c"),
    LIGHT_PURPLE("light_purple", "d"),
    YELLOW("yellow", "e"),
    WHITE("white", "f");
  }

  enum class ClickAction(val value: String) {
    OPEN_URL("open_url"),
    OPEN_FILE("open_file"),
    RUN_COMMAND("run_command"),
    SUGGEST_COMMAND("suggest_command"),
    CHANGE_PAGE("change_page"),
    COPY_TO_CLIPBOARD("copy_to_clipboard");
  }

  enum class TextDecoration(
    val value: String,
    val code: String
  ) : MCTextComponent {
    BOLD("bold", "l"),
    UNDECORATED("reset", "r"),
    ITALIC("italic", "o"),
    UNDERLINED("underlined", "n"),
    STRIKETHROUGH("strikethrough", "m"),
    OBFUSCATED("obfuscated", "k");
  }

  class ClickActionComponent(val action: ClickAction, val value: String) : MCTextComponent

  object NewLine : MCTextComponent

  companion object {
    val newLine = NewLine

    val bold: TextDecoration
      get() = TextDecoration.BOLD

    val italic: TextDecoration
      get() = TextDecoration.ITALIC

    val underlined: TextDecoration
      get() = TextDecoration.UNDERLINED

    val strikethrough: TextDecoration
      get() = TextDecoration.STRIKETHROUGH

    val obfuscated: TextDecoration
      get() = TextDecoration.OBFUSCATED

    val undecorated: TextDecoration
      get() = TextDecoration.UNDECORATED

    fun clickEvent(action: ClickAction, value: String): MCTextComponent {
      return ClickActionComponent(action, value)
    }
  }

  fun toJson(): String {
    var isBold = false
    var isItalic = false
    var isUnderlined = false
    var isStrikethrough = false
    var isObfuscated = false
    var color: Color? = null
    var clickEvent: ClickActionComponent? = null

    val jsonArray = JSONArray()
    this.parts.forEach {
      when (it) {
        is String -> {
          jsonArray.put(JSONObject().apply {
            put("text", it)

            if (isBold) put("bold", true)
            if (isItalic) put("italic", true)
            if (isUnderlined) put("underlined", true)
            if (isStrikethrough) put("strikethrough", true)
            if (isObfuscated) put("obfuscated", true)

            if (color != null) put("color", color!!.value)

            if (clickEvent != null) {
              put("clickEvent", JSONObject().apply {
                put("action", clickEvent!!.action.value)
                put("value", clickEvent!!.value)
              })
            }
          })
        }

        is TextDecoration -> {
          if (it == TextDecoration.BOLD) isBold = true
          if (it == TextDecoration.ITALIC) isItalic = true
          if (it == TextDecoration.UNDERLINED) isUnderlined = true
          if (it == TextDecoration.STRIKETHROUGH) isStrikethrough = true
          if (it == TextDecoration.OBFUSCATED) isObfuscated = true

          if (it == TextDecoration.UNDECORATED) {
            isBold = false
            isItalic = false
            isUnderlined = false
            isStrikethrough = false
            isObfuscated = false
          }
        }

        is Color -> {
          color = it
        }

        is ClickActionComponent -> {
          clickEvent = it
        }

        is NewLine -> {
          jsonArray.put(JSONObject().apply {
            put("text", "\n")
          })
        }
      }
    }

    return jsonArray.toString()
  }

  fun toCodedString(): String {
    val result = StringBuilder()
    this.parts.forEach {
      when (it) {
        is String -> {
          result.append(it)
        }

        is TextDecoration -> {
          result.append("\u00a7")
          result.append(it.code)
        }

        is Color -> {
          result.append("\u00a7")
          result.append(it.code)
        }

        is NewLine -> {
          result.append("\n")
        }
      }
    }

    return result.toString()
  }
}