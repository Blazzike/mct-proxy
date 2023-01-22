package util

import java.io.OutputStream

class OutputStreamBuffer(private val outputStream: OutputStream) : OutputStream() {
  private var buffer = ByteArray(0)
  val size
    get() = buffer.size

  override fun write(b: Int) {
    buffer += b.toByte()
  }

  override fun flush() {
    outputStream.write(buffer)
  }

  override fun toString(): String {
    return "Buffer(buffer=${buffer.contentToString()})"
  }
}

class Buffer(writer: Writer) : Writer(OutputStreamBuffer(writer.outputStream)) {
  val size
    get() = (outputStream as OutputStreamBuffer).size

  override fun toString(): String {
    return outputStream.toString()
  }

  fun flush() {
    (outputStream as OutputStreamBuffer).flush()
  }
}