package com.rezonation.services

import com.rezonation.types.events.ProcessArticleEvent
import zio.ZIO
import zio.ZLayer
import net.dankito.readability4j.Readability4J
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import scala.io.Source
class ArticleIngester() {
  // val browser = JsoupBrowser()

  // Highlevel TODO. Return the List of ArticleEntity to be stored in DB.
  // The insertion will happen in another service.
  def ingestArticles(events: List[ProcessArticleEvent]): ZIO[Any, Throwable, Unit] = {
    // Placeholder for actual ingestion logic
    ZIO.mergeAllPar(events.map(ingestArticle))(())((_, _) => ())
  }
  
  // TODO Get tags. 
  // 1. Extract Tags from URL
  // 2. Enhance with own tags via NLP parsing.
  // 3. Compose article to put into database. 

  private def ingestArticle(event: ProcessArticleEvent): ZIO[Any, Throwable, Unit] = {
    // Simulate ingestion logic, e.g., fetching and processing the article
    for {
      _      <- ZIO.succeed(println(s"Processing article from URL: ${event.url}"))
      source  = Source.fromURL(event.url, "UTF-8").mkString
      x       = new Readability4J(event.url, source)
      article = x.parse()
      _      <- ZIO.log(s"Title: ${article.getTitle}")
      _      <- ZIO.log(s"Author: ${article.getByline}")
      _      <- ZIO.log(s"Excerpt: ${article.getExcerpt}")
      y = article.getContent()
    } yield ()
  }
  /*
  import net.dankito.readability4j.Readability4J
import java.net.URL
import scala.io.Source

// Fetch raw HTML (you can use scala.io.Source, sttp, or Jsoup)
val url = "https://edition.cnn.com/2025/10/01/middleeast/analysis-trump-israel-gaza-peace-plan-latam-intl"
val html = Source.fromURL(url, "UTF-8").mkString

// Run Readability
val readability = new Readability4J(new URL(url), html)
val article = readability.parse()

// Extract fields
println(s"Title: ${article.getTitle}")
println(s"Author: ${article.getByline}")     // may be empty if missing
println(s"Excerpt: ${article.getExcerpt}")
println(s"Content snippet: ${article.getContent.take(300)}...")

   */
}

object ArticleIngester {
  def live: ZLayer[Any, Nothing, ArticleIngester] =
    ZLayer.succeed(new ArticleIngester())
}
