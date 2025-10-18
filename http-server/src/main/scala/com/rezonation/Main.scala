package com.rezonation

import com.rezonation.config.KafkaConfig
import com.rezonation.http.InternalServer
import com.rezonation.repositories.ArticlesRepository
import com.rezonation.services.ArticleService
import zio.*
import zio.kafka.producer.{Producer, ProducerSettings}

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
    (kafkaConfigLayer ++ producerLayer ++ ArticlesRepository.live) >>> ArticleService.live

  override def run: ZIO[Any & (ZIOAppArgs & Scope), Any, Any] = {
    ZIO
      .scoped { InternalServer.serve() }
      .provide(kafkaConfigLayer, articleServiceLayer)
  }
}
