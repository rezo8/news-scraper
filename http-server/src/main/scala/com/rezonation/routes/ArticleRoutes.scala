package com.rezonation.routes

import zio._
import zio.http._
import com.rezonation.services.ArticleService
import zio.http._

class ArticleRoutes(articleService: ArticleService) {
  val baseRoute = "articles"

  val routes = Routes(
    Method.GET / baseRoute             -> handler { (req: Request) =>
      for {
        processedArticles <- articleService.getAllProcessedArticles()
      } yield Response.text(processedArticles.mkString(", ")) // TODO Response pattern
    },
    Method.POST / baseRoute / "submit" -> handler { (req: Request) =>
      for {
        articleUrls <- ZIO.succeed(req.url.queryParams.getAll("url").toList)
        _           <- articleService.submitArticlesForProcessing(articleUrls)
      } yield Response.text("Article URLs submitted successfully")
    }
  )
}

// Usage with ZLayer
object ArticleRoutes {
  val live: ZLayer[ArticleService, Nothing, ArticleRoutes] =
    ZLayer.fromFunction(new ArticleRoutes(_))
}
