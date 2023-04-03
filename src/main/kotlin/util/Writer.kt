package util

import java.io.OutputStream
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.reflect.KClass

class EncryptedOutputStream(private val outputStream: OutputStream, sharedSecret: ByteArray) : OutputStream() {
  private val cipher: Cipher = Cipher.getInstance("AES/CFB8/NoPadding")
  private val iv = IvParameterSpec(sharedSecret.copyOfRange(0, 16))
  private val key = SecretKeySpec(sharedSecret, "AES")

  init {
    cipher.init(Cipher.ENCRYPT_MODE, key, iv)
  }

  override fun write(b: Int) {
    outputStream.write(cipher.update(byteArrayOf(b.toByte())))
  }

  override fun write(b: ByteArray, off: Int, len: Int) {
    if (b.size == 0) return

    val bytes = b.copyOfRange(off, off + len)
    val encrypted = cipher.update(bytes)
    outputStream.write(encrypted)
  }
}

open class Writer(var outputStream: OutputStream) {
  fun writeLong(value: Long) {
    for (i in 0..7) {
      writeByte((value shr (8 * (7 - i))).toInt())
    }
  }

  fun writeVarInt(value: Int) {
    var value = value
    while (true) {
      if (value and 0x7F.inv() == 0) {
        outputStream.write(value)
        return
      }

      outputStream.write(value and 0x7F or 0x80)
      value = value ushr 7
    }
  }

  fun writeString(value: String) {
    val byteArray = value.toByteArray()
    this.writeVarInt(byteArray.size)
    outputStream.write(byteArray)
  }

  fun writeVarLong(value: Long) {
    var value = value
    while (true) {
      if (value and 0x7F.inv() == 0L) {
        outputStream.write(value.toInt())
        return
      }

      outputStream.write((value and 0x7F or 0x80).toInt())
      value = value ushr 7
    }
  }

  fun writeHeader(packetId: Int, packetLength: Int) {
    this.writeVarInt(packetLength)
    this.writeVarInt(packetId)
  }

  fun writeBytes(bytes: ByteArray) {
    outputStream.write(bytes)
  }

  fun enableEncryption(sharedSecret: ByteArray) {
    outputStream = EncryptedOutputStream(outputStream, sharedSecret)
  }

  fun writeUUID(uuid: UUID) {
    writeLong(uuid.mostSignificantBits)
    writeLong(uuid.leastSignificantBits)
  }

  fun writeBoolean(boolean: Boolean) {
    outputStream.write(if (boolean) 1 else 0)
  }

  fun writeInt(int: Int) {
    outputStream.write(int shr 24)
    outputStream.write(int shr 16)
    outputStream.write(int shr 8)
    outputStream.write(int)
  }

  fun writeByte(byte: Int) {
    outputStream.write(byte)
  }

  fun writeShort(short: Int) {
    outputStream.write(short shr 8)
    outputStream.write(short)
  }

  fun writeByteArray(byteArray: ByteArray) {
    writeVarInt(byteArray.size)
    outputStream.write(byteArray)
  }

  fun writeEnums(enumClass: KClass<*>, enumSet: EnumSet<*>) {
    val size = enumClass.java.enumConstants.size
    val bitSet = BitSet(size)
    enumClass.java.enumConstants.forEachIndexed { index, enumConstant ->
      if (enumSet.contains(enumConstant)) {
        bitSet.set(index)
      }
    }

    outputStream.write(bitSet.toByteArray())
  }

  private fun writeLongArray(longs: LongArray) {
    writeVarInt(longs.size)
    longs.forEach { writeLong(it) }
  }

  fun writeBitSet(bitset: BitSet) {
    val bytes = bitset.toByteArray()
    writeVarInt(bytes.size)
    outputStream.write(bytes)
  }

  fun writeFloat(float: Float) {
    writeInt(float.toRawBits())
  }

  fun writeDouble(double: Double) {
    writeLong(double.toRawBits())
  }

  fun <T> writeArray(slotData: List<T>, transform: (T) -> Unit) {
    writeVarInt(slotData.size)
    slotData.forEach { transform(it) }
  }
}
