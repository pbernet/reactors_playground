import io.reactors._

/**
  * Implement the sample in Chapter "Functional composition of event streams"
  * http://reactors.io/tutorialdocs//reactors/event-streams/index.html
  *
  * Sandbox to shwow event streams. In "real life":
  * - Every event stream is bound to a specific `Reactor`, the basic unit of concurrency.
  *  Within a reactor, at most a single event stream propagates events at a time.
  *  An event stream will only produce events during the execution of that reactor --
  *  events are never triggered on a different reactor
  *  - It is forbidden to share event streams between reactors
  *  - Event streams are specialized for `Int`, `Long` and `Double` - TODO What are the implications if other types are used?
  */
object FunctionalCompositionOfEventStreams {

  def main(args: Array[String]) {

    val emitter = new Events.Emitter[Int]

    //Declare how to transform events into events for a derived event stream
    val sum = emitter.map(x => x * x).scanPast(0)(_ + _)
    sum.onEvent(x => println(s"Sum numbers: $x"))

    //Declare a union dataflow graph
    val even = emitter.filter(_ % 2 == 0)
    val odd = emitter.filter(_ % 2 == 1)
    val dataFlowGraph = even union odd
    dataFlowGraph.onEvent(x => println(s"Union output: $x"))

    //let it run
    for (i <- 0 until 5) emitter react i
  }
}
