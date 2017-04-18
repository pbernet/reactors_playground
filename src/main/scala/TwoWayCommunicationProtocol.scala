import io.reactors.{Reactor, ReactorSystem}
import io.reactors.protocol._

/**
  * Implement the sample in Chapter "2-Way Communication Protocol"
  * http://reactors.io/tutorialdocs//reactors/protocol-two-way/index.html
  *
  * xxxx
  * - xxxx
  * - xxxx
  */
object TwoWayCommunicationProtocol {

  def main(args: Array[String]) {
    val system = new ReactorSystem("test-system")

    val seeker = Reactor[Unit] { self =>
      val lengthServer = self.system.channels.twoWayServer[Int, String].serveTwoWay()

      lengthServer.links.onEvent { serverTwoWay =>
        serverTwoWay.input.onEvent { s =>
          serverTwoWay.output ! s.length
        }
      }

    lengthServer.channel.connect() onEvent { clientTwoWay =>
      clientTwoWay.output ! "What's my length?"
      clientTwoWay.input onEvent { len =>
        if (len == 17) println("clientTwoWay received correct reply")
        else println("clientTwoWay reply incorrect: " + len)
      }
    }
  }

  system.spawn(seeker)

  System.out.println("Press any key to terminate")
  System.in.read()
  System.out.println("Shutting down reactors system...")
  system.shutdown()
  }
}
