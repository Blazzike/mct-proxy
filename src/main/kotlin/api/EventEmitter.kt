package api

interface Event

open class EventEmitter<T : Event> {
  val listeners = mutableListOf<EventHandler<T>>()

  fun listen(listener: EventHandler<T>) : () -> Unit {
    listeners.add(listener)

    return {
      listeners.remove(listener)
    }
  }

  fun emit(event: T) {
    listeners.forEach { listener ->
      listener(event)
    }
  }
}

typealias EventHandler<T> = (T) -> Unit