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
    val ch = system.spawn(welcomeReactor)
    println(s"About to call ${ch.toString}")
    ch ! "Alan"
  }
}