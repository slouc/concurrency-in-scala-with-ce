import cats.effect.{ExitCode, IO, IOApp, Resource}

object Resources extends IOApp with PrintThread {

  def mkResource(s: String): Resource[IO, String] = {
    val acquire = IO(println(s"Acquiring $s")) *> IO.pure(s)
    def release(s: String) = IO(println(s"Releasing $s"))
    Resource.make(acquire)(release)
  }

  val r = for {
    outer <- mkResource("outer")
    inner <- mkResource("inner")
  } yield (outer, inner)

  override def run(args: List[String]): IO[ExitCode] =
    r.use { case (a, b) => IO(println(s"Using $a and $b")) }.map(_ => ExitCode.Success)

}

object ResourcesWithErrors extends IOApp with PrintThread {

  def mkResource(s: String): Resource[IO, String] = {
    val acquire = IO(println(s"Acquiring $s")) *> IO.pure(s)
    def release(s: String) = IO(println(s"Releasing $s"))
    Resource.make(acquire)(release)
  }

  val r = for {
    outer <- mkResource("outer")
    inner <- mkResource("inner")
    _ <- Resource.liftF(IO.raiseError(new Throwable("Boom!")))
  } yield (outer, inner)

  override def run(args: List[String]): IO[ExitCode] =
    r.use { case (a, b) => IO(println(s"Using $a and $b")) }.map(_ => ExitCode.Success)

}
