package util

import java.util.*


// from 00000000000000000000000000000000 to 00000000-0000-0000-0000-000000000000
fun uuidFromString(uuid: String): UUID {
  return UUID(
    uuid.substring(0, 16).toULong( 16).toLong(),
    uuid.substring(16).toULong( 16).toLong()
  )
}