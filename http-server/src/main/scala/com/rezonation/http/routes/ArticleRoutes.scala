package com.rezonation.http.routes

import com.rezonation.services.ArticleService
import com.rezonation.types.responses.FetchArticlesResponse
import zio.*
import zio.http.*
import zio.json.*

class ArticleRoutes(articleService: ArticleService) {
  val baseRoute = "articles"

  val routes: Routes[Any, Response] = Routes(
    Method.GET / baseRoute             -> handler { (req: Request) =>
      fetchProcessedArticles()
    },
    Method.POST / baseRoute / "submit" -> handler { (req: Request) =>
      {
        val articleUrls = req.url.queryParams.getAll("url").toList
        submitArticles(articleUrls) *> ZIO.succeed(
          Response.text("Articles submitted for processing")
        )
      }
    }
  )

  private def fetchProcessedArticles(): ZIO[Any, Nothing, Response] = {
    articleService.getAllProcessedArticles
      .map(articles => Response.text(FetchArticlesResponse(articles).toJson))
      .catchAll(error => ZIO.succeed(Response.error(Status.InternalServerError, error.getMessage)))
  }

  private def submitArticles(articleUrls: List[String]): ZIO[Any, Nothing, Unit] = {
    articleService
      .submitArticlesForProcessing(articleUrls)
      .catchAll(error => ZIO.succeed(Response.error(Status.InternalServerError, error.getMessage)))
  }
}

// Usage with ZLayer
object ArticleRoutes {
  val live: ZLayer[ArticleService, Nothing, ArticleRoutes] =
    ZLayer.fromFunction(new ArticleRoutes(_))

  def make(): ZIO[ArticleService, Nothing, ArticleRoutes] =
    ZIO.serviceWith[ArticleService](new ArticleRoutes(_))
}
