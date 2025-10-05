package com.rezonation.services

import com.rezonation.types.events.ProcessArticleEvent
import zio.ZIO
import zio.ZLayer
import net.dankito.readability4j.Readability4J
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import scala.io.Source
import net.dankito.readability4j.Article
class ArticleIngester(nlpProcessor: NLPProcessor) {

  // TODO write a test class for this.
  // Highlevel TODO. Return the List of ArticleEntity to be stored in DB.
  // The insertion will happen in another service.
  def ingestArticles(events: List[ProcessArticleEvent]): ZIO[Any, Throwable, Unit] = {
    // Placeholder for actual ingestion logic
    ZIO.mergeAllPar(events.map(ingestArticle))(())((_, _) => ())
  }

  // TODO: Compose article to put into database.

  private def ingestArticle(event: ProcessArticleEvent): ZIO[Any, Throwable, Unit] = {
    // Simulate ingestion logic, e.g., fetching and processing the article
    for {
      _      <- ZIO.log(s"Processing article from URL: ${event.url}")
      source  = Source.fromURL(event.url, "UTF-8").mkString
      x       = new Readability4J(event.url, source)
      article = x.parse()
      tags   <- extractTags(article)
      // Compose processed article to prepare for DB
    } yield ()
  }

  // Steps:
  // Get Tags from article (i.e keyword. Look at the articles to see if we can find it)
  // Lets integrate Spark NLP
  // Run TF-IDF to get more tags, up to 10 combined
  // Return
  private def extractTags(article: Article): ZIO[Any, Throwable, List[String]] = {
    for {
      tags <- nlpProcessor.extractKeywords(article.getContent(), 10)
    } yield tags
  }
}

object ArticleIngester {
  def live: ZLayer[Any, Nothing, ArticleIngester] =
    ZLayer.succeed(new ArticleIngester(new NLPProcessorImpl()))
}
