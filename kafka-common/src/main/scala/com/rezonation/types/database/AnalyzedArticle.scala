package com.rezonation.types.database

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

import java.util.UUID
import java.util.UUID.randomUUID

final case class AnalyzedArticle(
    tags: List[String],
    url: String,
    title: String,
    articleText: String,
    id: UUID = randomUUID()
)

object AnalyzedArticle {
  implicit val encoder: JsonEncoder[AnalyzedArticle] = DeriveJsonEncoder.gen[AnalyzedArticle]
  implicit val decoder: JsonDecoder[AnalyzedArticle] = DeriveJsonDecoder.gen[AnalyzedArticle]
}
