package com.rezonation.types.responses

import com.rezonation.types.database.AnalyzedArticle
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class FetchArticlesResponse(
    articles: List[AnalyzedArticle]
)

object FetchArticlesResponse {
  implicit val encoder: JsonEncoder[FetchArticlesResponse] =
    DeriveJsonEncoder.gen[FetchArticlesResponse]
  implicit val decoder: JsonDecoder[FetchArticlesResponse] =
    DeriveJsonDecoder.gen[FetchArticlesResponse]
}
