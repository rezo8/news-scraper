package com.rezonation.repositories

import com.rezonation.clients.Elastic4sClient
import com.rezonation.types.database.AnalyzedArticle
import com.sksamuel.elastic4s.{CommonRequestOptions, ElasticClient}
import com.sksamuel.elastic4s.ElasticDsl.*
import zio.*
import zio.json.*

class ArticlesRepository(client: ElasticClient) {
  private val indexName = "articles"

  // We ONLY create here. Since differing content will result in a differentId, there is no collision.
  def indexArticles(articles: List[AnalyzedArticle]): ZIO[Any, Throwable, Unit] =
    ZIO.fromFuture { implicit ec =>
      val bulkRequests = articles.map { article =>
        indexInto(indexName)
          .id(article.id.toString)
          .doc(article.toJson)
      }
      client
        .execute {
          bulk(bulkRequests)
        }
        .map(_ => ())
    }

  def getAllArticles: ZIO[Any, Throwable, List[AnalyzedArticle]] =
    searchArticles("*")

  private def searchArticles(query: String): ZIO[Any, Throwable, List[AnalyzedArticle]] =
    ZIO.fromFuture { implicit ec =>
      client
        .execute {
          search(indexName).query(query)
        }
        .map { response =>
          response.result.hits.hits.toList.flatMap { hit =>
            hit.sourceAsString.fromJson[AnalyzedArticle].toOption
          }
        }
    }
}

object ArticlesRepository {
  def live: ZLayer[Any, Throwable, ArticlesRepository] =
    ZLayer.fromZIO {
      for {
        client <- Elastic4sClient.createClient
      } yield new ArticlesRepository(client)
    }
}
