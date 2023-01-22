package util

import packets.Packet
import packets.PacketInfo
import java.io.InputStream
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.reflect.KClass

private const val SEGMENT_BITS = 0x7F
private const val CONTINUE_BIT = 0x80

class EncryptedInputStream(private val inputStream: InputStream, private val sharedSecret: ByteArray) : InputStream() {
  private val cipher: Cipher = Cipher.getInstance("AES/CFB8/NoPadding")
  private val iv = IvParameterSpec(sharedSecret.copyOfRange(0, 16))
  private val key = SecretKeySpec(sharedSecret, "AES")

  init {
    cipher.init(Cipher.DECRYPT_MODE, key, iv)
  }

  override fun read(): Int {
    return inputStream.read().let { if (it == -1) -1 else cipher.update(byteArrayOf(it.toByte()))[0].toInt() }
  }
}

class Reader(var inputStream: InputStream) {
  fun readLong(): Long {
    val bytes = ByteArray(8)
    inputStream.read(bytes)

    return bytes.iterator().asSequence().map { it.toLong() and 0xFF }.reduce { acc, byte ->
      (acc shl 8) or byte
    }
  }

  fun readVarLong(): Long {
    var value: Long = 0
    var position = 0
    var currentByte: Int

    while (true) {
      currentByte = inputStream.read()
      value = value or ((currentByte and SEGMENT_BITS).toLong() shl position)
      if (currentByte and CONTINUE_BIT == 0) break
      position += 7
      if (position >= 64) throw RuntimeException("VarLong is too big")
    }

    return value
  }

  fun readVarInt(): Int {
    var value = 0
    var position = 0
    var currentByte: Int

    while (true) {
      currentByte = inputStream.read()
      value = value or (currentByte and SEGMENT_BITS shl position)
      if (currentByte and CONTINUE_BIT == 0) break
      position += 7
      if (position >= 32) throw RuntimeException("VarInt is too big")
    }

    return value
  }

  fun readString(): String {
    val length = this.readVarInt()
    val bytes = ByteArray(length)
    inputStream.read(bytes)

    return String(bytes)
  }

  fun readUnsignedShort(): Short {
    val byte1 = inputStream.read()
    val byte2 = inputStream.read()
    return ((byte1 shl 8) + byte2).toShort()
  }

  class Header(var packetLength: Int, var packetId: Int) {
    val remainingLength: Int
      get() = packetLength - 1

    override fun toString(): String {
      return "Header(packetId=0x${packetId.toString(16)}, packetLength=$packetLength)"
    }
  }

  fun readHeader(): Header {
    return Header(this.readVarInt(), this.readByte())
  }

  fun readByte(): Int {
    return inputStream.read()
  }

  fun <P : Packet> expectPacket(packetInfo: PacketInfo<P>, header: Header = this.readHeader()): P {
    val expectedPacketId = packetInfo.id
    if (header.packetId != expectedPacketId) {
      throw RuntimeException("Expected packet $expectedPacketId but got ${header.packetId}")
    }

    val packetData = this.inputStream.readNBytes(header.remainingLength)

    return packetInfo.packetClass.java.getDeclaredConstructor().newInstance().apply {
      this.read(Reader(packetData.inputStream()))
    }
  }

  fun readBoolean(): Boolean {
    return inputStream.read() == 1
  }

  fun readUUID(): UUID? {
    val mostSignificantBits = readLong()
    val leastSignificantBits = readLong()
    return UUID(mostSignificantBits, leastSignificantBits)
  }

  fun readByteArray(length: Int = readVarInt()): ByteArray {
    val bytes = ByteArray(length)
    inputStream.read(bytes)

    return bytes
  }

  fun enableEncryption(sharedSecret: ByteArray) {
    inputStream = EncryptedInputStream(inputStream, sharedSecret)
  }

  fun readLongArray(length: Int = readVarInt()): LongArray {
    val array = LongArray(length)
    for (i in 0 until length) {
      array[i] = this.readLong()
    }

    return array
  }

  fun <T : Enum<T>> readEnums(enumClass: KClass<T>): EnumSet<T> {
    val length = enumClass.java.enumConstants.size
    val bytes = ByteArray(length / 8 + 1)
    inputStream.read(bytes)

    val bitSet = BitSet.valueOf(bytes)
    val enumSet = EnumSet.noneOf(enumClass.java)
    for (i in 0 until length) {
      if (bitSet.get(i)) {
        enumSet.add(enumClass.java.enumConstants[i])
      }
    }

    return enumSet
  }

  fun readDouble(): Double {
    return Double.fromBits(readLong())
  }
}