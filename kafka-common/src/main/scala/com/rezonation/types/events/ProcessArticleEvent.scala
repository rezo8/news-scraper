package com.rezonation.types.events

import zio.json.*
import zio.kafka.serde.Serde
import zio.ZIO

import java.util.UUID

final case class ProcessArticleEvent(
    url: String,
    correlationId: String = UUID.randomUUID().toString
) extends BaseEvent[ProcessArticleEvent] {

  override def applyCorrelationId(correlationId: String): ProcessArticleEvent =
    copy(correlationId = correlationId)
}

object ProcessArticleEvent {

  def apply(url: String): ZIO[Any, Nothing, ProcessArticleEvent] =
    BaseEvent
      .constructEvent(() => ProcessArticleEvent(url))

  implicit val codec: JsonCodec[ProcessArticleEvent] = DeriveJsonCodec.gen[ProcessArticleEvent]

  implicit val decoder: JsonDecoder[ProcessArticleEvent] =
    DeriveJsonDecoder.gen[ProcessArticleEvent]
  implicit val encoder: JsonEncoder[ProcessArticleEvent] =
    DeriveJsonEncoder.gen[ProcessArticleEvent]

  implicit val serde: Serde[Any, ProcessArticleEvent] =
    Serde.string.inmapZIO[Any, ProcessArticleEvent](s =>
      ZIO.fromEither(s.fromJson[ProcessArticleEvent]).mapError(new RuntimeException(_))
    )(evt => ZIO.succeed(evt.toJson))

}
