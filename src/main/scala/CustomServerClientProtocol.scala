import io.reactors.{Reactor, ReactorSystem, _}

/**
  * Implement the sample in Chapter "Custom Server-Client Protocol"
  * http://reactors.io/tutorialdocs//reactors/protocol-intro/index.html
  *
  * Not very useful to start the server-client protocol inside a single reactor.
  * Normally, the server and the client are separated by the network, or are at least different reactors running inside the same reactor system.
  */
object CustomServerClientProtocol {

  type Req[T, S] = (T, Channel[S]) //The client must not only send the request value to the server, but also send it a channel used for the reply
  type Server[T, S] = Channel[Req[T, S]] //The Server type is a channel that accepts request objects

  def main(args: Array[String]) {
    val system = new ReactorSystem("test-system")

    def server[T, S](f: T => S): Server[T, S] = {
      //a connector is a pair of channel (= actor references) and event stream
      val reply: Connector[(T, Channel[S])] = system.channels.open[Req[T, S]]
      reply.events onMatch {
        case (x, reply) => reply ! f(x)
      }
      reply.channel
    }

    //The client protocol. Define a new method ? on the Channel type, which sends the request to the server.
    //This method cannot immediately return the server’s response, because the response arrives asynchronously.
    //Instead, ? must return an event stream with the server’s reply.
    //So, the ? method must create a reply channel, send the Req object to the server, and then return the event stream associated with the reply channel
    implicit class ChannelOps[T, S: Arrayable](val server: Server[T, S]) {
      def ?(payload: T): Events[S] = {
        val connector = system.channels.daemon.open[S]
        server ! (payload, connector.channel)
        connector.events
      }
    }

    val serverClientInOne = Reactor[Unit] { self =>
      val s = server[String, String](_.toUpperCase)
      (s ? "hello") onEvent { upper =>
        println(s"Reply from server: $upper")
      }
    }

    system.spawn(serverClientInOne)

    System.out.println("Press any key to terminate")
    System.in.read()
    System.out.println("Shutting down reactors system...")
    system.shutdown()
  }
}
