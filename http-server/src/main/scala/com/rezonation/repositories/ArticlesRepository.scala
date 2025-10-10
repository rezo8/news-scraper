package com.rezonation.services

import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.ElasticDsl._
import zio._
import zio.json._

case class Article(id: String, title: String, content: String, tags: List[String])

object Article {
  implicit val encoder: JsonEncoder[Article] = DeriveJsonEncoder.gen[Article]
  implicit val decoder: JsonDecoder[Article] = DeriveJsonDecoder.gen[Article]
}

class Elastic4sService(client: ElasticClient) {

  private val indexName = "articles"

  def indexArticle(article: Article): ZIO[Any, Throwable, Unit] =
    ZIO.succeed(()) // Placeholder for actual implementation
    // ZIO.fromFuture { implicit ec =>
    //   client.execute {
    //     indexInto(indexName).id(article.id).doc(article)
    //   }.map(_ => ())
    // }

  def searchArticles(query: String): ZIO[Any, Throwable, List[Article]] =
    ZIO.fromFuture { implicit ec =>
      client
        .execute {
          search(indexName).query(query)
        }
        .map { response =>
          response.result.hits.hits.toList.flatMap { hit =>
            hit.sourceAsString.fromJson[Article].toOption
          }
        }
    }
}

object Elastic4sService {
  def live: ZLayer[ElasticClient, Nothing, Elastic4sService] =
    ZLayer.fromFunction(new Elastic4sService(_))
}
