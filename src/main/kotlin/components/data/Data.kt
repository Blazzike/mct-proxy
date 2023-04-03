package components.data

import api.Component
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.io.File

const val DATA_DIRECTORY = "data"

object Data : Component() {
  fun readFileContents(path: String): String {
    File(DATA_DIRECTORY, path).let { file ->
      if (!file.exists()) {
        file.parentFile.mkdirs()
        file.createNewFile()

        return ""
      }

      return file.readText()
    }
  }

  fun readJsonObjectFile(path: String): JsonObject {
    return readFileContents(path).let {
      if (it.isEmpty()) {
        JsonObject()
      } else {
        Gson().fromJson(it, JsonObject::class.java)
      }
    }
  }
}