import cats.effect.{ExitCode, IO, IOApp}

object RunWithoutFiber extends IOApp {

  def io(i: Int): IO[Unit] = IO({
    Thread.sleep(3000)
    println(s"Hi from $i!")
  })

  val program = for {
    startTime <- IO(System.currentTimeMillis())
    _ <- io(1)
    _ <- io(2)
    endTime <- IO(System.currentTimeMillis())
    _ <- IO(println(s"Elapsed: ${endTime - startTime} ms"))
  } yield ExitCode.Success

  override def run(args: List[String]): IO[ExitCode] = program

  // Hi from 1!
  // Hi from 2!
  // Elapsed: 6077 ms
}