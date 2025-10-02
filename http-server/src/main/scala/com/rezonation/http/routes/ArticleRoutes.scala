package com.rezonation.http.routes

import zio._
import zio.http._
import com.rezonation.services.ArticleService
import zio.http._

class ArticleRoutes(articleService: ArticleService) {
  val baseRoute = "articles"

  val routes: Routes[Any, Response] = Routes(
    Method.GET / baseRoute             -> handler { (req: Request) =>
      for {
        processedArticles <- articleService.getAllProcessedArticles()
      } yield Response.text(processedArticles.mkString(", ")) // TODO Response pattern
    },
    Method.POST / baseRoute / "submit" -> handler { (req: Request) =>
      {
        val articleUrls = req.url.queryParams.getAll("url").toList
        for {
          _ <- articleService
                 .submitArticlesForProcessing(articleUrls)
                 .catchAll(error =>
                   ZIO.succeed(Response.error(Status.InternalServerError, error.getMessage))
                 )
        } yield Response.text("Articles submitted for processing")
      }
    }
  )
}

// Usage with ZLayer
object ArticleRoutes {
  val live: ZLayer[ArticleService, Nothing, ArticleRoutes] =
    ZLayer.fromFunction(new ArticleRoutes(_))

  def make(): ZIO[ArticleService, Nothing, ArticleRoutes] =
    ZIO.serviceWith[ArticleService](new ArticleRoutes(_))
}
