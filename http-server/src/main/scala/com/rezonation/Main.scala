package com.rezonation

import zio._
import zio.http._

import com.rezonation.config.KafkaConfig
import zio.kafka.producer.Producer
import zio.kafka.producer.ProducerSettings
import com.rezonation.services.ArticleService
import com.rezonation.http.routes.ArticleRoutes

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

  override def run: ZIO[Any & (ZIOAppArgs & Scope), Any, Any] = {
    ZIO
      .scoped {
        for {
          articleRoutes <- ZIO.service[ArticleRoutes]
          app            = articleRoutes.routes
          _             <- Server.serve(app).provide(Server.defaultWithPort(8080))
        } yield ()
      }
      .provide(kafkaConfigLayer, articleServiceLayer, articleRoutesLayer)
  }
}
