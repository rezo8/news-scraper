package com.rezonation

import com.rezonation.repositories.ArticlesRepository
import com.rezonation.services.ArticleIngestion
import com.rezonation.types.events.ProcessArticleEvent
import zio.*
import zio.kafka.consumer.*
import zio.kafka.consumer.Consumer.{AutoOffsetStrategy, OffsetRetrieval}
import zio.kafka.serde.*

object Main extends ZIOAppDefault {

  private val articlesRepositoryLayer                         = ArticlesRepository.live
  override def run: ZIO[Any & (ZIOAppArgs & Scope), Any, Any] = {
    ZIO
      .scoped {
        for {
          articleAnalyzer <- ZIO.service[ArticleIngestion]
          // TODO Make this consumer poolable.
          consumer        <- Consumer.make(
                               ConsumerSettings(List("localhost:9092"))
                                 .withGroupId("articleConsumer")
                                 .withOffsetRetrieval(OffsetRetrieval.Auto(AutoOffsetStrategy.Latest))
                             )
          _               <-
            consumer
              .plainStream(
                Subscription.topics("article-events"),
                Serde.string,
                ProcessArticleEvent.serde
              )
              .groupedWithin(100, 1.second)
              .mapZIO { recordBatch =>
                articleAnalyzer
                  .ingestArticles(recordBatch.map(_.record.value).toList)
                  .flatMap(articles =>
                    ZIO.logInfo(
                      s"Ingested ${articles.length} articles. URLS: ${articles.map(_.url).mkString(", ")}"
                    )
                  )
                  .as(recordBatch.map(_.offset))
                  // TODO this fails on 30 second timeout when ES is down which is much too long.
                  .tapError(error =>
                    ZIO.logError(s"Failed to ingest articles: ${error.getMessage}")
                  )
                  .retry(Schedule.forever)
              }
              .mapZIO(offsets => OffsetBatch(offsets).commit)
              .runDrain
          _               <- ZIO.logInfo("Kafka Consumer started")
        } yield ()
      }
      .provide(articlesRepositoryLayer, ArticleIngestion.live)
  }
}
