package com.rezonation.services
import zio.*
import zio.http.*
import zio.*
import zio.kafka.consumer.*
import zio.kafka.producer.{Producer, ProducerSettings}
import zio.kafka.serde.*
import zio.stream.ZStream
import org.apache.kafka.clients.producer.ProducerRecord
import com.rezonation.config.KafkaConfig
import com.rezonation.types.events.ProcessArticleEvent
import com.rezonation.repositories.ArticlesRepository
import com.rezonation.types.database.AnalyzedArticle

// TODO make topic be in config.
class ArticleService(
    config: KafkaConfig,
    producer: Producer,
    articlesRepository: ArticlesRepository
) {
  def submitArticlesForProcessing(articles: List[String]): ZIO[Any, Throwable, Unit] = {
    for {
      _ <- ZStream
             .fromIterable(articles)
             .mapZIOChunked(article =>
               ZIO.succeed(new ProducerRecord(config.topic, ProcessArticleEvent(article)))
             )
             .via(producer.produceAll(Serde.int, ProcessArticleEvent.serde))
             .runDrain
    } yield ()
  }

  def getAllProcessedArticles: ZIO[Any, Throwable, List[AnalyzedArticle]] = {
    for {
      articles <- articlesRepository.getAllArticles
    } yield articles
  }
}

object ArticleService {

  def live: ZLayer[KafkaConfig & Producer & ArticlesRepository, Nothing, ArticleService] =
    ZLayer.fromFunction(new ArticleService(_, _, _))

}
