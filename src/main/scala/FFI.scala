import FFI._
import cats.effect.{Async, Concurrent, ExitCode, IO, IOApp}
import cats.implicits._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object FFI {

  case class User(id: String)

  // this is the asynchronous process that we want to translate
  // from the "foreign" world into our IO-based code
  def fetchUser(userId: String): Future[User] = Future(User(userId))
}

object AsyncExample extends IOApp {

  def fromFuture[A](future: => Future[A]): IO[A] =
    IO.async { cb =>
      future.onComplete {
        case Success(a) => cb(Right(a))
        case Failure(e) => cb(Left(e))
      }
    }

  override def run(args: List[String] = List()): IO[ExitCode] =
    for {
      user <- fromFuture(fetchUser("User_42"))
      _ <- IO(println(s"User ID = ${user.id}"))
    } yield ExitCode.Success
}

object CancelableExample extends IOApp {

  def fromFutureAsync[A](future: => Future[A]): IO[A] =
    IO.async { cb =>
      future.onComplete {
        case _ => // don't use the callback!
      }
      // this will never happen bcs we used `async` instead of `cancelable`!
      IO(println("Rollback the transaction!"))
    }

  def fromFutureCancelable[A](future: => Future[A]): IO[A] =
    IO.cancelable { cb =>
      future.onComplete {
        case _ => // don't use the callback!
      }
      IO(println("Rollback the transaction!"))
    }

  // also try out fromFutureAsync!
  override def run(args: List[String] = List()): IO[ExitCode] =
    for {
      user1 <- fromFutureCancelable(fetchUser("User_42"))
      _ <- IO(println(s"User ID = ${user1.id}"))
    } yield ExitCode.Success

}
