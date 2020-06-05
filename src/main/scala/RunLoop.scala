import cats.effect.{ExitCode, IO}

import scala.io.StdIn.readLine

object RunLoop {

  sealed trait IO[+A]
  case class FlatMap[B, +A](io: IO[B], k: B => IO[A]) extends IO[A]
  case class Pure[+A](v: A) extends IO[A]
  case class Delay[+A](eff: () => A) extends IO[A]

  val program = for {
    _ <- IO(println(s"What's up?"))
    input <- IO(readLine)
    _ <- IO(println(s"Ah, $input is up!"))
  } yield ExitCode.Success

  val stack: FlatMap[String, Unit] = FlatMap(
    FlatMap(
      Delay(() => print("What's up?")),
      (_: Unit) => Delay(() => readLine)
    ),
    input => Delay(() => println(s"Ah, $input is up!"))
  )
}
