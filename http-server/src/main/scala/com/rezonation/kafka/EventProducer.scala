package com.rezonation.kafka

import zio._
import zio.kafka.consumer._
import zio.kafka.producer.{Producer, ProducerSettings}
import zio.kafka.serde._
import zio.stream.ZStream
import org.apache.kafka.clients.producer.RecordMetadata

object EventProducer {
  val producerScope: ZIO[Scope, Throwable, Producer] =
    Producer.make(
      ProducerSettings(List("localhost:9092"))
    )

  def produceRecord(
      producer: Producer,
      topic: String,
      key: Long,
      value: String
  ): RIO[Any, RecordMetadata] =
    producer.produce[Any, Long, String](
      topic = topic,
      key = key,
      value = value,
      keySerializer = Serde.long,
      valueSerializer = Serde.string
    )
}
