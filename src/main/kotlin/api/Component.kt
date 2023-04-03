package api

open class Component {
  open fun enable() {
    //
  }

  open fun disable() {
    //
  }

  fun log(message: String) {
    println("[${this::class.simpleName}] $message")
  }
}