package com.rezonation

import zio._
import zio.kafka.consumer._
import zio.kafka.serde._
import zio.*
import zio.kafka.consumer.{Consumer, ConsumerSettings}

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
          consumer <- ZIO.service[Consumer]
          _        <- consumer
              .plainStream(Subscription.topics("article-events"), Serde.string, Serde.string) 
              .tap(cr => ZIO.log(s"key: ${cr.record.key}, value: ${cr.record.value}"))        
              .map(_.offset)                                                                  
              .aggregateAsync(Consumer.offsetBatches)                                         
              .mapZIO(_.commit)                                                               
              .runDrain
          _        <- ZIO.logInfo("Kafka Consumer started")
        } yield ()
      }
      .provide(consumerLayer)
  }
}
