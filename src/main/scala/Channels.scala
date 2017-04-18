import io.reactors.{Reactor, ReactorSystem}
import scala.collection._
import io.reactors._

trait Op[K, V]
case class Put[K, V](k: K, v: V) extends Op[K, V]
case class Get[K, V](k: K, ch: Channel[V]) extends Op[K, V]

class MapReactor[K, V] extends Reactor[Op[K, V]] {
  val map: mutable.Map[K, V] = mutable.Map[K, V]()
  main.events onEvent {
    case Put(k, v) => map(k) = v
    case Get(k, ch) => ch ! map(k)
  }
}

/**
  * Implement the sample in Chapter "Using channels"
  * http://reactors.io/tutorialdocs//reactors/reactors/index.html
  *
  */
object Channels {
  def main(args: Array[String]) {
    val system = new ReactorSystem("test-system")

    val mapper: Channel[Op[String, List[String]]] = system.spawn(Proto[MapReactor[String, List[String]]])
    mapper ! Put("dns-main", "dns1" :: "lan" :: Nil)
    mapper ! Put("dns-backup", "dns2" :: "com" :: Nil)

    val clientReactorChannel: Channel[String] = system.spawn(Reactor[String] { self =>
      val log = system.log
      log.apply("Logger started")

      self.main.events onMatch {
        case "start" =>
          val reply: Connector[List[String]] = self.system.channels.daemon.open[List[String]]
          log.apply("About to call Get")
          val key = "dns-main"
          mapper ! Get(key, reply.channel)
          reply.events onEvent { (url: Seq[String]) =>
            log.apply(s"Result list for key: $key is: $url")
          }
        case "end" =>
          self.main.seal()
        case event@_ => log.apply(s"Unknown event: $event")
      }
    })
    
    clientReactorChannel ! "start"

    System.out.println("Press any key to terminate")
    System.in.read()
    System.out.println("Shutting down reactors system...")
    clientReactorChannel ! "end"
    system.shutdown()
  }
}
