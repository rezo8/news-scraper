package com.rezonation.services
import zio._
import zio.http._
import zio._
import zio.kafka.consumer._
import zio.kafka.producer.{Producer, ProducerSettings}
import zio.kafka.serde._
import zio.stream.ZStream
import org.apache.kafka.clients.producer.ProducerRecord
import com.rezonation.config.KafkaConfig
import com.rezonation.types.events.ProcessArticleEvent

// TODO make topic be in config.
class ArticleService(config: KafkaConfig, producer: Producer) {
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

  def getAllProcessedArticles(): ZIO[Any, Nothing, List[String]] = {
    ZIO.succeed(List("article1", "article2", "article3"))
  }
}

object ArticleService {

  def live: ZLayer[KafkaConfig & Producer, Nothing, ArticleService] =
    ZLayer.fromFunction(new ArticleService(_, _))

}
