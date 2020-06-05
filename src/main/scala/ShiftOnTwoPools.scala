import java.util.concurrent.Executors

import cats.effect.{ContextShift, ExitCode, IO, IOApp}

import scala.concurrent.ExecutionContext

object ShiftOnTwoPools extends IOApp with PrintThread {

  val ec1 = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))
  val ec2 = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))

  val cs1: ContextShift[IO] = IO.contextShift(ec1)
  val cs2: ContextShift[IO] = IO.contextShift(ec2)

  def loop(id: String)(i: Int): IO[Unit] =
    for {
      _ <- IO(printThread(id))
      _ <- if (i == 10) IO.shift(cs1) else IO.unit
      _ <- IO(Thread.sleep(200))
      result <- loop(id)(i + 1)
    } yield result

  val program = for {
    _ <- loop("A")(0).start(cs1)
    _ <- loop("B")(0).start(cs2)
  } yield ExitCode.Success

  override def run(args: List[String]): IO[ExitCode] = program
}

object ShiftOnTwoPoolsWithFallback extends IOApp with PrintThread {

  val ec1 = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))
  val ec2 = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))
  val ec3 = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))

  val cs: ContextShift[IO] = IO.contextShift(ec1)

  def io(s: String) = IO(println(s"$s: ${Thread.currentThread.getName}"))

  val r = cs
    .evalOn(ec2)(io("A").flatMap(_ =>
      cs.evalOn(ec3)(io("B1")).flatMap(_ => io("B2"))
    ))

  override def run(args: List[String]): IO[ExitCode] =
    r.map(_ => ExitCode.Success)
}
