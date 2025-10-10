package com.rezonation.services

import com.rezonation.types.events.ProcessArticleEvent
import zio.ZIO
import zio.ZLayer
import net.dankito.readability4j.Readability4J
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import scala.io.Source
import net.dankito.readability4j.Article
import com.rezonation.types.database.AnalyzedArticle

class ArticleAnalyzer(nlpProcessor: NLPProcessor) {

  // TODO write a test class for this.
  // The insertion will happen in another service.
  def ingestArticles(
      events: List[ProcessArticleEvent]
  ): ZIO[Any, Throwable, List[AnalyzedArticle]] = {
    ZIO
      .mergeAllPar(events.map(ingestArticle))(List.empty[AnalyzedArticle])((x, y) => x :+ y)
      .tapError(e => ZIO.logError(s"Error ingesting articles: ${e.getMessage}"))
  }

  private def ingestArticle(event: ProcessArticleEvent): ZIO[Any, Throwable, AnalyzedArticle] = {
    for {
      _      <- ZIO.log(s"Processing article from URL: ${event.url}")
      source  = Source.fromURL(event.url, "UTF-8").mkString
      x       = new Readability4J(event.url, source)
      article = x.parse()
      tags   <- extractTags(article)
      _      <- ZIO.log(s"Extracted tags: ${tags.mkString(", ")}")
      // Compose processed article to prepare for DB
    } yield AnalyzedArticle(tags, event.url, article.getTitle(), article.getContent())
  }

  private def extractTags(
      article: Article,
      keywordCount: Int = 10
  ): ZIO[Any, Throwable, List[String]] = {
    for {
      tags <- nlpProcessor.extractKeywords(article.getContent(), keywordCount)
    } yield tags
  }
}

object ArticleAnalyzer {
  def live: ZLayer[Any, Nothing, ArticleAnalyzer] =
    ZLayer.succeed(new ArticleAnalyzer(new NLPProcessorImpl()))
}
