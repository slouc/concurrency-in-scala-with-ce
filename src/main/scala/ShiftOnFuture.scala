import java.util.concurrent.Executors

import cats.effect.ExitCode

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

object ShiftOnFuture extends App with PrintThread {

  implicit val ec =
    ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))

  def loop(id: String)(i: Int): Future[Unit] =
    for {
      _ <- Future(printThread(id))
      _ <- Future(Thread.sleep(200))
      result <- loop(id)(i + 1)
    } yield result

  val program = for {
    _ <- loop("A")(0)
    _ <- loop("B")(0)
  } yield ExitCode.Success

  Await.result(program, Duration.Inf)
}
