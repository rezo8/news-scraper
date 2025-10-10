package com.rezonation

import zio._
import zio.kafka.consumer._
import zio.kafka.serde._
import zio.*
import zio.kafka.consumer.{Consumer, ConsumerSettings}
import com.rezonation.types.events.ProcessArticleEvent
import com.rezonation.services.ArticleAnalyzer

object Main extends ZIOAppDefault {

  val consumerLayer: Layer[Any, Consumer] =
    ZLayer.scoped { // (1)
      val consumerSettings: ConsumerSettings =
        ConsumerSettings(List("localhost:9092")).withGroupId("group")
      Consumer.make(consumerSettings)
    }

  override def run: ZIO[Any & (ZIOAppArgs & Scope), Any, Any] = {
    ZIO
      .scoped {
        for {
          articleAnalyzer <- ZIO.service[ArticleAnalyzer]
          consumer        <- ZIO.service[Consumer]
          _               <- consumer
                               .plainStream(
                                 Subscription.topics("article-events"),
                                 Serde.string,
                                 ProcessArticleEvent.serde
                               )
                               .groupedWithin(100, 1.second)
                               .tap(records =>
                                 records.mapZIO(r => ZIO.log(s"key: ${r.record.key}, value: ${r.record.value}"))
                               )
                               .mapZIO { recordBatch =>
                                 {
                                   val events = recordBatch.map(_.record.value).toList
                                   articleAnalyzer
                                     .ingestArticles(events)
                                     .map(articles => {
                                       articles.foreach(article =>
                                         println(
                                           s"Analyzed Article: URL=${article.url}, Title=${article.title}, Tags=${article.tags
                                               .mkString(", ")}"
                                         )
                                       )
                                     })
                                     .as(recordBatch.map(_.offset))
                                 }

                               }
                               .mapZIO(offsets => OffsetBatch(offsets).commit)
                               .runDrain
          _               <- ZIO.logInfo("Kafka Consumer started")
        } yield ()
      }
      .provide(consumerLayer, ArticleAnalyzer.live)
  }
}
