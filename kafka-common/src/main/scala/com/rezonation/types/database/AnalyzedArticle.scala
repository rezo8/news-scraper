package com.rezonation.types.database

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

import java.security.MessageDigest
import java.util.UUID
import java.util.UUID.randomUUID

final case class AnalyzedArticle(
    tags: List[String],
    url: String,
    title: String,
    articleText: String,
    id: UUID = randomUUID(),
    createdAt: Long = System.currentTimeMillis(),
    updatedAt: Long = System.currentTimeMillis()
)

object AnalyzedArticle {
  def apply(
      tags: List[String],
      url: String,
      title: String,
      articleText: String
  ): AnalyzedArticle = {
    val md5       = MessageDigest.getInstance("MD5")
    val hashBytes = md5.digest(articleText.getBytes("UTF-8"))
    val bb        = java.nio.ByteBuffer.wrap(hashBytes.take(16))
    val id        = new UUID(bb.getLong, bb.getLong)
    AnalyzedArticle(tags, url, title, articleText, id)
  }

  implicit val encoder: JsonEncoder[AnalyzedArticle] = DeriveJsonEncoder.gen[AnalyzedArticle]
  implicit val decoder: JsonDecoder[AnalyzedArticle] = DeriveJsonDecoder.gen[AnalyzedArticle]
}
