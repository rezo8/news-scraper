package com.rezonation.services
import com.rezonation.config.KafkaConfig
import com.rezonation.repositories.ArticlesRepository
import com.rezonation.types.database.AnalyzedArticle
import com.rezonation.types.events.ProcessArticleEvent
import org.apache.kafka.clients.producer.ProducerRecord
import zio.*
import zio.kafka.producer.Producer
import zio.kafka.serde.*
import zio.stream.ZStream

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
               for {
                 processArticleEvent <- ProcessArticleEvent(article)
               } yield new ProducerRecord(config.topic, processArticleEvent)
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
