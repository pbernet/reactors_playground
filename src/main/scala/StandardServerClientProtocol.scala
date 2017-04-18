import io.reactors.ReactorSystem
import io.reactors.protocol._

/**
  * Implement the sample in Chapter "Standard Server-Client Protocol"
  * http://reactors.io/tutorialdocs//reactors/protocol-intro/index.html
  *
  */
object StandardServerClientProtocol {

  def main(args: Array[String]) {
    val system = new ReactorSystem("test-system")

    //create and start a server reactor
    val server = system.server[Int, Int]{(state, x) =>
      //state.subscription.unsubscribe()
      println(s"state: $state")  //The state object for the server contains the Subscription object, which allows clients to stop the server if an unexpected event arrives.
      x * 2}

    system.spawnLocal[Unit] { self =>
      (server ? 7) onEvent { response =>
        println(s"Response from server $response")
      }

      (server ? 7) onEvent { response =>
        println(s"Response from server $response")
      }
    }


    System.out.println("Press any key to terminate")
    System.in.read()
    System.out.println("Shutting down reactors system...")
    system.shutdown()
  }
}
