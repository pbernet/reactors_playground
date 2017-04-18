import io.reactors._

/**
  * Implement the sample in Chapter "Higher-order event streams"
  * http://reactors.io/tutorialdocs//reactors/event-streams/index.html
  *
  */
object HigherOrderEventStreams {

  def main(args: Array[String]) {

    val currentEvents = new Events.Emitter[Events[Int]]
    val e1 = new Events.Emitter[Int]
    val e2 = new Events.Emitter[Int]
    //The mux operator multiplexes events from the last Events[T] – whenever a new nested event stream is emitted,
    //events from previously emitted nested event streams are ignored, and only the events from the latest nested event stream get forwarded.
    val currentEvent = currentEvents.mux.filter(x => x % 2 == 0)
    val subscription = currentEvent.onEvent(println)

    currentEvents.react(e1)
    e2.react(1) // nothing is printed
    e1.react(2) // 2 is printed
    e1.react(3) // nothing is printed, caught in even filter
    currentEvents.react(e2)
    e2.react(6) // 6 is printed
    e1.react(7) // nothing is printed

    //The client is responsible for unsubscribing from an event source once the events ar no longer needed.
    //Failure to do so risks *time leaks*, a form of resource leak in which the program gets slower and slower (because it needs to propagate no longer needed events).
    subscription.unsubscribe()
  }
}
