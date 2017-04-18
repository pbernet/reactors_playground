import io.reactors.protocol._
import io.reactors.{Reactor, ReactorSystem}

/**
  * Implement the sample in Chapter "Two-Way Server Reactors"
  * http://reactors.io/tutorialdocs//reactors/protocol-two-way/index.html
  *
  * Enhancements
  * - Use a ResultContainer instead of a Double
  * - Add a filter in the result event stream
  */
object TwoWayServerReactors {

  case class ResultContainer(value: Double, valueString: String)

  def main(args: Array[String]) {
    val system = new ReactorSystem("test-system")

    val seriesCalculator = Reactor.twoWayServer[ResultContainer, Int] {
      server =>
        server.links onEvent { twoWay =>
          twoWay.input onEvent { n =>
            if (n <= 0) {
              twoWay.subscription.unsubscribe()
            } else {
              for (i <- 1 until n) {
                val result = 1.0 / i
                twoWay.output ! ResultContainer(result, result.toString)
              }
            }
          }
        }
    }
    val server = system.spawn(seriesCalculator)

    //client
    system.spawnLocal[Unit] { self =>
      server.connect() onEvent { twoWay =>
        twoWay.output ! 4
        twoWay.input.filter(x => x.value <= 0.5) onEvent { x =>
          println(x)
          //If you want to terminate after the 1st result
          //twoWay.subscription.unsubscribe()
        }
      }
    }

  System.out.println("Press any key to terminate")
  System.in.read()
  System.out.println("Shutting down reactors system...")
  system.shutdown()
  }
}
