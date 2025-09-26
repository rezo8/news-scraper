import zio._

object HelloWorld extends ZIOAppDefault {
  def run: ZIO[Any, Nothing, Unit] =
    for {
      _ <- Console.printLine("Hello, ZIO World!").orDie
    } yield ()
}
