package com.rezonation.services

import com.rezonation.types.events.ProcessArticleEvent
import zio.ZIO
import zio.ZLayer

class ArticleIngester() {

  def ingestArticles(events: List[ProcessArticleEvent]): ZIO[Any, Throwable, Unit] = {
    // Placeholder for actual ingestion logic
    ZIO.mergeAllPar(events.map(ingestArticle))(())((_, _) => ())
  }

  private def ingestArticle(event: ProcessArticleEvent): ZIO[Any, Throwable, Unit] = {
    // Simulate ingestion logic, e.g., fetching and processing the article
    ZIO.succeed(println(s"Processing article from URL: ${event.url}"))
  }
}

object ArticleIngester {
  def live: ZLayer[Any, Nothing, ArticleIngester] =
    ZLayer.succeed(new ArticleIngester())
}
