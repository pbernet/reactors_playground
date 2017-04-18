import io.reactors.{Reactor, ReactorSystem}

import io.reactors._
import scala.concurrent.duration._
import scala.concurrent.Promise

/**
  * Implement the sample in Chapter "The channels service"
  * http://reactors.io/tutorialdocs//reactors/services/index.html
  *
  * This example creates two reactors:
  * - The first reactor will create a specially named channel after some delay
  * - The second reactor will await that named channel. When the channel appears, the second reactor will send an event to that channel.
  */
object ChannelsService {
  def main(args: Array[String]) {
    val system = new ReactorSystem("test-system")

    val done = Promise[Boolean]()

    val firstReactor = Reactor[String] { self =>
      val log = system.log
      log.apply("firstReactor created")
      system.clock.timeout(1.second) on {
        val ch = system.channels.daemon.named("lucky").open[Int]
        ch.events onEvent  { anInteger =>
          log.apply(s"received $anInteger")
          done.success(true)
          self.main.seal()
        }
      }
    }

    system.spawn(firstReactor.withName("first"))
    system.spawn(Reactor[String] { self =>
      system.channels.await[Int]("first", "lucky") onEvent { luckyChannel =>
        luckyChannel ! 7
        self.main.seal()
      }
    })

    System.out.println("Press any key to terminate")
    System.in.read()
    System.out.println("Shutting down reactors system...")
    system.shutdown()
  }
}
