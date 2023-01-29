package components.commands.packets.client

import packets.BoundTo
import packets.Packet
import packets.PacketInfo
import util.Buffer
import util.Reader
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

open class Node(
  private val nodeType: NodeType,
  val isExecutable: Boolean,
  var children: MutableList<Int>,
) {
  enum class NodeType(val id: Int) {
    ROOT(0),
    LITERAL(1),
    ARGUMENT(2);
  }

  open val flag: Int
    get() = nodeType.id or (if (isExecutable) 0x04 else 0)

  open fun write(buffer: Buffer) {
    buffer.writeByte(flag)

    buffer.writeVarInt(children.size)
    children.forEach { buffer.writeVarInt(it) }
  }

  companion object {
    fun read(buffer: Reader): Node {
      val flag = buffer.readByte()

      val childrenSize = buffer.readVarInt()
      val children = (0 until childrenSize).map { buffer.readVarInt() }.toMutableList()

      val result: Node
      if (flag and 0x02 != 0) {
        val redirectNode = if (flag and 0x08 != 0) buffer.readVarInt() else null
        val name = buffer.readString()
        val paserId = buffer.readVarInt()
        val parserType = ParserType.values()[paserId]
        val parser = parserType.parserClass.createInstance().readProperties(buffer)
        parser.parserType = parserType

        result = ArgumentNode(
          isExecutable = flag and 0x04 != 0,
          redirectNode = redirectNode,
          name = name,
          children = children,
          parser = parser,
          suggestionType = if (flag and 0x10 != 0) suggestionTypeByIdentifier[buffer.readString()] else null,
        )
      } else if (flag and 0x01 != 0) {
        val redirectNode = if (flag and 0x08 != 0) buffer.readVarInt() else null
        val name = buffer.readString()
        result = LiteralNode(
          isExecutable = flag and 0x04 != 0,
          redirectNode = redirectNode,
          name = name,
          children = children,
        )
      } else {
        result = RootNode(
          children = children,
        )
      }

      return result
    }
  }

  override fun toString(): String {
      return "Node(nodeType=$nodeType, isExecutable=$isExecutable, children=$children)"
  }
}

class RootNode(
  children: MutableList<Int>,
) : Node(
  nodeType = NodeType.ROOT,
  isExecutable = false,
  children = children,
)

class LiteralNode(
  val name: String,
  isExecutable: Boolean,
  children: MutableList<Int>,
  val redirectNode: Int? = null,
) : Node(
  nodeType = NodeType.LITERAL,
  isExecutable = isExecutable,
  children = children,
) {
  override val flag: Int
    get() = super.flag or
        (if (redirectNode != null) 0x08 else 0)

  override fun write(buffer: Buffer) {
    super.write(buffer)
    if (redirectNode != null) {
      buffer.writeVarInt(redirectNode)
    }

    buffer.writeString(name)
  }

  override fun toString(): String {
    return "LiteralNode(name='$name', isExecutable=$isExecutable, children=$children, redirectNode=$redirectNode)"
  }
}

open class Parser(var parserType: ParserType? = null) {
  open fun writeProperties(buffer: Buffer) {
    //
  }

  open fun readProperties(buffer: Reader): Parser {
    return this
  }

  override fun toString(): String {
    return "Parser(parserType=$parserType)"
  }
}

class ParserFloat(
  var min: Float? = null,
  var max: Float? = null,
) : Parser(ParserType.FLOAT) {
  override fun writeProperties(buffer: Buffer) {
    val flag = (if (min != null) 0x01 else 0) or
        (if (max != null) 0x02 else 0)

    buffer.writeByte(flag)
    if (min != null) {
      buffer.writeFloat(min!!)
    }

    if (max != null) {
      buffer.writeFloat(max!!)
    }
  }

  override fun readProperties(buffer: Reader): ParserFloat {
    val flag = buffer.readByte()
    if (flag and 0x01 != 0) {
      min = buffer.readFloat()
    }

    if (flag and 0x02 != 0) {
      max = buffer.readFloat()
    }

    return this
  }

  override fun toString(): String {
    return "ParserFloat(min=$min, max=$max)"
  }
}

class ParserDouble(
  var min: Double? = null,
  var max: Double? = null,
) : Parser(ParserType.DOUBLE) {
  override fun writeProperties(buffer: Buffer) {
    val flag = (if (min != null) 0x01 else 0) or
        (if (max != null) 0x02 else 0)

    buffer.writeByte(flag)
    if (min != null) {
      buffer.writeDouble(min!!)
    }

    if (max != null) {
      buffer.writeDouble(max!!)
    }
  }

  override fun readProperties(buffer: Reader): ParserDouble {
    val flag = buffer.readByte()
    if (flag and 0x01 != 0) {
      min = buffer.readDouble()
    }

    if (flag and 0x02 != 0) {
      max = buffer.readDouble()
    }

    return this
  }

  override fun toString(): String {
    return "ParserDouble(min=$min, max=$max)"
  }
}

class ParserInteger(
  var min: Int? = null,
  var max: Int? = null,
) : Parser(ParserType.INTEGER) {
  override fun writeProperties(buffer: Buffer) {
    val flag = (if (min != null) 0x01 else 0) or
        (if (max != null) 0x02 else 0)

    buffer.writeByte(flag)
    if (min != null) {
      buffer.writeInt(min!!)
    }

    if (max != null) {
      buffer.writeInt(max!!)
    }
  }

  override fun readProperties(buffer: Reader): ParserInteger {
    val flag = buffer.readByte()
    if (flag and 0x01 != 0) {
      min = buffer.readInt()
    }

    if (flag and 0x02 != 0) {
      max = buffer.readInt()
    }

    return this
  }

  override fun toString(): String {
    return "ParserInteger(min=$min, max=$max)"
  }
}

class ParserLong(
  var min: Long? = null,
  var max: Long? = null,
) : Parser(ParserType.LONG) {
  override fun writeProperties(buffer: Buffer) {
    val flag = (if (min != null) 0x01 else 0) or
        (if (max != null) 0x02 else 0)

    buffer.writeByte(flag)
    if (min != null) {
      buffer.writeVarLong(min!!)
    }

    if (max != null) {
      buffer.writeVarLong(max!!)
    }
  }

  override fun readProperties(buffer: Reader): ParserLong {
    val flag = buffer.readByte()
    if (flag and 0x01 != 0) {
      min = buffer.readVarLong()
    }

    if (flag and 0x02 != 0) {
      max = buffer.readVarLong()
    }

    return this
  }

  override fun toString(): String {
    return "ParserLong(min=$min, max=$max)"
  }
}

class ParserString(
  var type: ParserStringType = ParserStringType.SINGLE_WORD,
) : Parser(ParserType.STRING) {
  enum class ParserStringType {
    SINGLE_WORD,
    QUOTABLE_PHRASE,
    GREEDY_PHRASE,
  }

  override fun writeProperties(buffer: Buffer) {
    buffer.writeByte(type.ordinal)
  }

  override fun readProperties(buffer: Reader): ParserString {
    type = ParserStringType.values()[buffer.readByte()]

    return this
  }

  override fun toString(): String {
    return "ParserString(type=$type)"
  }
}

class ParserEntity(
  var isOnlyPlayers: Boolean = true,
  var isSingle: Boolean = true,
) : Parser(ParserType.ENTITY) {
  override fun writeProperties(buffer: Buffer) {
    val flag = (if (isSingle) 0x01 else 0) or
        (if (isOnlyPlayers) 0x02 else 0)

    buffer.writeByte(flag)
  }

  override fun readProperties(buffer: Reader): ParserEntity {
    val flag = buffer.readByte()
    isSingle = flag and 0x01 != 0
    isOnlyPlayers = flag and 0x02 != 0

    return this
  }

  override fun toString(): String {
    return "ParserEntity(isOnlyPlayers=$isOnlyPlayers, isSingle=$isSingle)"
  }
}

class ParserScoreHolder(
  var allowMultiple: Boolean = false,
) : Parser(ParserType.SCORE_HOLDER) {
  override fun writeProperties(buffer: Buffer) {
    buffer.writeByte(if (allowMultiple) 0x01 else 0)
  }

  override fun readProperties(buffer: Reader): ParserScoreHolder {
    allowMultiple = buffer.readByte() and 0x01 != 0

    return this
  }

  override fun toString(): String {
    return "ParserScoreHolder(allowMultiple=$allowMultiple)"
  }
}

open class ResourceParser(parserType: ParserType? = null, var identifier: String? = null) : Parser(parserType) {
  override fun writeProperties(buffer: Buffer) {
    if (identifier == null) {
      throw IllegalStateException("Identifier is null")
    }

    buffer.writeString(identifier!!)
  }

  override fun readProperties(buffer: Reader): ResourceParser {
    identifier = buffer.readString()

    return this
  }

  override fun toString(): String {
    return "ResourceParser(identifier=$identifier)"
  }
}

class ParserResourceOrTag(identifier: String? = null) : ResourceParser(ParserType.RESOURCE_OR_TAG, identifier)
class ParserResourceOrTagKey(identifier: String? = null) : ResourceParser(ParserType.RESOURCE_OR_TAG_KEY, identifier)
class ParserResource(identifier: String? = null) : ResourceParser(ParserType.RESOURCE, identifier)
class ParserResourceKey(identifier: String? = null) : ResourceParser(ParserType.RESOURCE_KEY, identifier)

enum class ParserType(val id: Int, val parserClass: KClass<out Parser> = Parser::class) {
  BOOL(0),
  FLOAT(1, ParserFloat::class),
  DOUBLE(2, ParserDouble::class),
  INTEGER(3, ParserInteger::class),
  LONG(4, ParserLong::class),
  STRING(5, ParserString::class),
  ENTITY(6, ParserEntity::class),
  GAME_PROFILE(7),
  BLOCK_POS(8),
  COLUMN_POS(9),
  VEC3(10),
  VEC2(11),
  BLOCK_STATE(12),
  BLOCK_PREDICATE(13),
  ITEM_STACK(14),
  ITEM_PREDICATE(15),
  COLOR(16),
  COMPONENT(17),
  MESSAGE(18),
  NBT(19),
  NBT_TAG(20),
  NBT_PATH(21),
  OBJECTIVE(22),
  OBJECTIVE_CRITERIA(23),
  OPERATION(24),
  PARTICLE(25),
  ANGLE(26),
  ROTATION(27),
  SCOREBOARD_SLOT(28),
  SCORE_HOLDER(29, ParserScoreHolder::class),
  SWIZZLE(30),
  TEAM(31),
  ITEM_SLOT(32),
  RESOURCE_LOCATION(33),
  FUNCTION(34),
  ENTITY_ANCHOR(35),
  INT_RANGE(36),
  FLOAT_RANGE(37),
  DIMENSION(38),
  GAME_MODE(39),
  TIME(40),
  RESOURCE_OR_TAG(41, ParserResourceOrTag::class),
  RESOURCE_OR_TAG_KEY(42, ParserResourceOrTagKey::class),
  RESOURCE(43, ParserResource::class),
  RESOURCE_KEY(44, ParserResourceKey::class),
  TEMPLATE_MIRROR(45),
  TEMPLATE_ROTATION(46),
  UUID(47);
}

enum class SuggestionType(val identifier: String) {
  ASK_SERVER("minecraft:ask_server"),
  ALL_RECIPES("minecraft:all_recipes"),
  AVAILABLE_SOUNDS("minecraft:available_sounds"),
  AVAILABLE_BIOMES("minecraft:available_biomes"),
  SUMMONABLE_ENTITIES("minecraft:summonable_entities");
}

val suggestionTypeByIdentifier = SuggestionType.values().associateBy { it.identifier }

class ArgumentNode(
  val name: String,
  isExecutable: Boolean,
  children: MutableList<Int>,
  val parser: Parser,
  val redirectNode: Int? = null,
  val suggestionType: SuggestionType? = null,
) : Node(
  nodeType = NodeType.ARGUMENT,
  isExecutable = isExecutable,
  children = children,
) {
  override val flag: Int
    get() = super.flag or
        (if (redirectNode != null) 0x08 else 0) or
        (if (suggestionType != null) 0x10 else 0)

  override fun write(buffer: Buffer) {
    if (parser.parserType == null) {
      throw IllegalArgumentException("Parser type is null")
    }

    super.write(buffer)
    if (redirectNode != null) {
      buffer.writeVarInt(redirectNode)
    }

    buffer.writeString(name)
    buffer.writeVarInt(parser.parserType!!.id)
    parser.writeProperties(buffer)
    if (suggestionType != null) {
      buffer.writeString(suggestionType.identifier)
    }
  }

  override fun toString(): String {
    return "ArgumentNode(name='$name', isExecutable=$isExecutable, children=$children, parser=$parser, redirectNode=$redirectNode, suggestionType=$suggestionType)"
  }
}

class CommandsPacket(
  var nodes: MutableList<Node> = mutableListOf(),
  var rootIndex: Int? = null
) : Packet() {
  companion object : PacketInfo<CommandsPacket>(0x0E, BoundTo.CLIENT)

  public override fun _write(buffer: Buffer) {
    buffer.writeVarInt(nodes.size)
    nodes.forEach { it.write(buffer) }
    buffer.writeVarInt(rootIndex!!)
  }

  override fun read(reader: Reader): Packet {
    var count = reader.readVarInt()
    nodes = (0 until count).map {
      Node.read(reader)
    }.toMutableList()

    rootIndex = reader.readVarInt()

    return this
  }

  override fun additionalParams(): Map<String, Any?> {
    return mapOf(
      "nodes" to nodes,
      "rootIndex" to rootIndex,
    )
  }
}