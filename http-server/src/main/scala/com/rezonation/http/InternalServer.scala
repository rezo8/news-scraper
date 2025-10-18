package com.rezonation.http

import com.rezonation.http.routes.ArticleRoutes
import com.rezonation.logging.LoggingMiddleware
import com.rezonation.services.ArticleService
import zio.*
import zio.http.Server

object InternalServer {

  def serve(): ZIO[ArticleService, Throwable, Nothing] = {
    for {
      _              <- ZIO.logInfo("Starting Internal HTTP Server on port 8080")
      articleService <- ZIO.service[ArticleService]
      articleRoutes  <- ArticleRoutes.make().provide(ZLayer.succeed(articleService))
      allRoutes       = { (articleRoutes.routes) @@ LoggingMiddleware.defaultMiddlewares }
      proc           <- Server.serve(allRoutes).provide(Server.defaultWithPort(8080))
    } yield proc
  }
}
