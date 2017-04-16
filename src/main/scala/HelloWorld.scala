import io.reactors._

object HelloWorld {
  def main(args: Array[String]) {
    val welcomeReactor = Reactor[String] {
      self =>
        println(s"About to instantiate: $self")
        self.main.events.onEvent { (name: String) =>
          println(s"Welcome, $name!")
          self.main.seal()
        }
    }
    val system = ReactorSystem.default("test-system")

    //start the welcomeReactor of a thread pool
    //val ch = system.spawn(welcomeReactor)

    //start the welcomeReactor on a dedicated thread to prevent it from dying
    val ch = system.spawn(welcomeReactor.withScheduler(JvmScheduler.Key.newThread))

    println(s"About to call $ch")
    ch ! "Alan"

//    needed if welcomeReactor is started of a thread pool
//    System.out.println("Press any key to terminate")
//    System.in.read()
//    System.out.println("Shutting down reactors system...")
//    system.shutdown()
  }
}