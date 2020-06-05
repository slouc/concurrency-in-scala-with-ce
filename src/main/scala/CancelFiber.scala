import java.util.concurrent.Executors

import cats.effect.{ContextShift, ExitCode, Fiber, IO, IOApp, Resource}
import cats.implicits._

import scala.concurrent.ExecutionContext

object CancelFiberWithLeak extends IOApp {

  val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))
  implicit val cs: ContextShift[IO] = IO.contextShift(ec)

  val f1 = for {
    f1 <- IO(Thread.sleep(1000)).start
    _ <- f1.join
    _ <- IO(println("Joined f1"))
  } yield ()

  val f2 = for {
    f2 <- IO.raiseError[Unit](new Throwable("boom!")).start
    _ <- f2.join
    _ <- IO(println("Joined f2"))
  } yield ()

  val program = (f1, f2).parMapN {
    case _ => ExitCode.Success
  }

  override def run(args: List[String] = List()): IO[ExitCode] = program
}

object CancelFiberSafely extends IOApp {

  val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))
  implicit val cs: ContextShift[IO] = IO.contextShift(ec)

  def safeStart[A](id: String)(io: IO[A]): Resource[IO, Fiber[IO, A]] =
    Resource.make(io.start)(fiber => fiber.cancel >> IO(println(s"Joined $id")))

  val r1 = safeStart("1")(IO(Thread.sleep(1000)))
  val r2 = safeStart("2")(IO.raiseError[Unit](new Throwable("boom!")))

  val program = (r1.use(_.join), r2.use(_.join)).parMapN {
    case _ => ExitCode.Success
  }

  override def run(args: List[String] = List()): IO[ExitCode] = program
}
