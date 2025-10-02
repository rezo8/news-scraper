package com.rezonation.types.events

import zio.json._
import zio.kafka.serde.Serde
import zio.ZIO

final case class ProcessArticleEvent(
    url: String
)

object ProcessArticleEvent {
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
