import cats.effect.{ExitCode, IO, IOApp}

object RunWithFiber extends IOApp {

  def io(i: Int): IO[Unit] = IO({
    Thread.sleep(3000)
    println(s"Hi from $i!")
  })

  val program = for {
    startTime <- IO(System.currentTimeMillis())
    fiber <- io(1).start
    _ <- io(2)
    _ <- fiber.join
    endTime <- IO(System.currentTimeMillis())
    _ <- IO(println(s"Elapsed: ${endTime - startTime} ms"))
  } yield ExitCode.Success

  override def run(args: List[String] = List()): IO[ExitCode] = program

  // Hi from 1!
  // Hi from 2!
  // Elapsed: 3371 ms
}