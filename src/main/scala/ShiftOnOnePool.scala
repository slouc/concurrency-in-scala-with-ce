import cats.effect.{ContextShift, ExitCode, IO, IOApp}

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

object ShiftOnOneThread extends IOApp with PrintThread {

  val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))
  val cs: ContextShift[IO] = IO.contextShift(ec)

  def loop(id: String)(i: Int): IO[Unit] =
    for {
      _ <- IO(printThread(id))
      _ <- IO.shift(cs) // <--- now we shift!
      _ <- IO(Thread.sleep(200))
      result <- loop(id)(i + 1)
    } yield result

  val program = for {
    _ <- loop("A")(0).start(cs)
    _ <- loop("B")(0).start(cs)
  } yield ExitCode.Success

  override def run(args: List[String]): IO[ExitCode] = program
}

object ShiftOnTwoThreads extends IOApp with PrintThread {

  val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))
  val cs: ContextShift[IO] = IO.contextShift(ec)

  def loop(id: String)(i: Int): IO[Unit] =
    for {
      _ <- IO(printThread(id))
      _ <- IO(Thread.sleep(200))
      result <- loop(id)(i + 1)
    } yield result

  val program = for {
    _ <- loop("A")(0).start(cs)
    _ <- loop("B")(0).start(cs)
  } yield ExitCode.Success

  override def run(args: List[String]): IO[ExitCode] = program
}
