package util

import models.Position
import packets.Packet
import packets.PacketInfo
import java.io.EOFException
import java.io.InputStream
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

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

  override fun read(b: ByteArray, off: Int, len: Int): Int {
    return inputStream.read(b, off, len).let { if (it == -1) -1 else cipher.update(b, off, it, b, off) }
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
      currentByte = readByte()
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
      currentByte = readByte()
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

  fun readShort(): Short {
    val byte1 = readByte()
    val byte2 = readByte()
    return ((byte1 shl 8) + byte2).toShort()
  }

  class Header(var packetLength: Int, var packetId: Int) {
    val remainingBytes: Int
      get() = packetLength - 1

    override fun toString(): String {
      return "Header(packetId=0x${packetId.toString(16)}, packetLength=$packetLength)"
    }
  }

  fun readHeader(): Header {
    return Header(this.readVarInt(), this.readByte())
  }

  fun readByte(): Int {
    val byte = inputStream.read()
    if (byte == -1) throw EOFException()

    return byte
  }

  fun <P : Packet> expectPacket(packetInfo: PacketInfo<P>, header: Header = this.readHeader()): P {
    val expectedPacketId = packetInfo.id
    if (header.packetId != expectedPacketId) {
      throw RuntimeException("Expected packet $expectedPacketId but got ${header.packetId}")
    }

    val packetData = inputStream.readNBytes(header.remainingBytes)

    return packetInfo.packetClass.java.getDeclaredConstructor().newInstance().apply {
      this.read(Reader(packetData.inputStream()))
    }
  }

  fun readBoolean(): Boolean {
    return readByte() == 1
  }

  fun readUUID(): UUID {
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

  fun readDouble(): Double {
    return Double.fromBits(readLong())
  }

  fun readFloat(): Float {
    return Float.fromBits(readInt())
  }

  fun readInt(): Int {
    val bytes = ByteArray(4)
    inputStream.read(bytes)

    return bytes.iterator().asSequence().map { it.toInt() and 0xFF }.reduce { acc, byte ->
      (acc shl 8) or byte
    }
  }

  fun readPosition(): Position {
    val value = readLong()

    return Position(
      x = value shr 38,
      y = value shl 52 shr 52,
      z = value shl 26 shr 38
    )
  }

  fun skipNBT(type: Int = readByte()) {
    when (type) {
      0 -> return
      1 -> inputStream.skipNBytes(1)
      2 -> inputStream.skipNBytes(2)
      3 -> inputStream.skipNBytes(4)
      4 -> inputStream.skipNBytes(8)
      5 -> inputStream.skipNBytes(4)
      6 -> inputStream.skipNBytes(8)
      7 -> {
        val length = readInt().toLong()
        inputStream.skipNBytes(length)
      }
      8 -> {
        val length = readShort().toLong()
        inputStream.skipNBytes(length)
      }
      9 -> {
        val type = readByte()
        val length = readInt().toLong()
        for (i in 0 until length) {
          skipNBT(type)
        }
      }
      10 -> {
        while (true) {
          val type = readByte()
          if (type == 0) return
          skipNBT(8) // string
          skipNBT(type)
        }
      }
      11 -> {
        val length = readInt()
        inputStream.skipNBytes((length * 4).toLong())
      }
      12 -> {
        val length = readInt()
        inputStream.skipNBytes((length * 8).toLong())
      }
      else -> throw RuntimeException("Unknown NBT type $type")
    }
  }

  inline fun <reified T> readArray(length: Int = readVarInt(), transform: () -> T): List<T> {
    return List(length) { transform() }
  }

  fun readBitSet(): BitSet {
    val length = readVarInt()
    val bytes = readByteArray(length)
    val bitSet = BitSet(length * 8)
    for (i in 0 until length) {
      for (j in 0 until 8) {
        if (bytes[i].toInt() and (1 shl j) != 0) {
          bitSet.set(i * 8 + j)
        }
      }
    }
    return bitSet
  }
}