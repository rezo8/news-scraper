// import zio._
// import zio.http._

// import com.rezonation.config.KafkaConfig
// import zio.kafka.producer.Producer
// import zio.kafka.producer.ProducerSettings
// import com.rezonation.services.ArticleService

// object Main extends ZIOAppDefault {

//   val kafkaConfigLayer = ZLayer.succeed(KafkaConfig(List("localhost:9092"), "article-events"))

//   // --------------- Producer Layer ---------------
//   val producerLayer: ZLayer[KafkaConfig, Throwable, Producer] =
//     ZLayer.scoped {
//       for {
//         cfg      <- ZIO.service[KafkaConfig]
//         producer <- Producer.make(ProducerSettings(cfg.bootstrapServers))
//       } yield producer
//     }

//   val articleServiceLayer: ZLayer[KafkaConfig, Throwable, ArticleService] =
//     (kafkaConfigLayer ++ producerLayer) >>> ArticleService.live

//     // --------------- ArticleRoutes Layer ----------
//   val articleRoutesLayer: ZLayer[Any, Throwable, ArticleRoute] =
//     articleServiceLayer >>> ArticleRoutes.live

//   val routes =
//     Routes(
//       Method.GET / Root    -> handler(Response.text("Greetings at your service")),
//       Method.GET / "greet" -> handler { (req: Request) =>
//         val name = req.queryOrElse[String]("name", "World")
//         Response.text(s"Hello $name!")
//       }
//     )

//   def run = Server.serve(routes).provide(Server.default)
// }

import zio._
import zio.http._

import com.rezonation.config.KafkaConfig
import zio.kafka.producer.Producer
import zio.kafka.producer.ProducerSettings
import com.rezonation.services.ArticleService
import com.rezonation.routes.ArticleRoutes

object Main extends ZIOAppDefault {

  val kafkaConfigLayer = ZLayer.succeed(KafkaConfig(List("localhost:9092"), "article-events"))

  // --------------- Producer Layer ---------------
  val producerLayer: ZLayer[KafkaConfig, Throwable, Producer] =
    ZLayer.scoped {
      for {
        cfg      <- ZIO.service[KafkaConfig]
        producer <- Producer.make(ProducerSettings(cfg.bootstrapServers))
      } yield producer
    }

  val articleServiceLayer: ZLayer[KafkaConfig, Throwable, ArticleService] =
    (kafkaConfigLayer ++ producerLayer) >>> ArticleService.live

  val articleRoutesLayer: ZLayer[KafkaConfig & ArticleService, Throwable, ArticleRoutes] =
    articleServiceLayer >>> ArticleRoutes.live

  // val allRoutes: HttpApp[Any, Throwable] =
  //   Http.collectZIO[Request] { req =>
  //     for {
  //       articleRoutes <- ZIO.service[ArticleRoutes]
  //       resp          <- articleRoutes.routes(req)
  //     } yield resp
  //   }

  // override def run: ZIO[Scope, Throwable, Any] = {
  //   ZIO
  //     .scoped {
  //       for {
  //         articleRoutes <- ZIO.service[ArticleRoutes]
  //         app            = articleRoutes.routes
  //         _             <- Server.serve(app)
  //       } yield ()
  //     }
  //     .provide(
  //       kafkaConfigLayer,
  //       producerLayer,
  //       articleServiceLayer,
  //       articleRoutesLayer
  //     )
  // }

  override def run: ZIO[Scope, Throwable, Any] =
    (kafkaConfigLayer >>> ArticleService.live >>> ArticleRoutes.live).build.use { env =>
      val articleRoutes = env.get[ArticleRoutes]
      Server
        .serve(articleRoutes.routes)
        .provide(Server.default)
    }
}
