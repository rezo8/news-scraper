package com.rezonation.services

import com.rezonation.repositories.ArticlesRepository
import com.rezonation.types.database.AnalyzedArticle
import com.rezonation.types.events.ProcessArticleEvent
import net.dankito.readability4j.{Article, Readability4J}
import zio.{Scope, ZIO, ZLayer}

import scala.io.Source
import scala.language.postfixOps

class ArticleIngestion(nlpProcessor: NLPProcessor, articlesRepository: ArticlesRepository) {

  // TODO write a test class for this.
  def ingestArticles(
      events: List[ProcessArticleEvent]
  ): ZIO[Any, Throwable, List[AnalyzedArticle]] = {
    ZIO.scoped {
      ZIO
        .mergeAllPar(events.map(ingestArticle))(List.empty[AnalyzedArticle])((x, y) => x :+ y)
        .flatMap(analyzedArticles => {
          articlesRepository.indexArticles(analyzedArticles).map(_ => analyzedArticles)
        })
        .tapError(e => ZIO.logError(s"Error ingesting articles: ${e.getMessage}"))
    }
  }

  private def ingestArticle(event: ProcessArticleEvent): ZIO[Scope, Throwable, AnalyzedArticle] =
    ZIO.logAnnotate("correlation_id", event.correlationId) {
      ZIO
        .acquireRelease(ZIO.attempt(Source.fromURL(event.url, "UTF-8")))(source =>
          ZIO.attempt(source.close()).orDie
        )
        .flatMap { source =>
          for {
            _       <- ZIO.logInfo(s"Processing article from URL: ${event.url}")
            content <- ZIO.attempt(source.mkString)
            article  = new Readability4J(event.url, content).parse()
            tags    <- extractTags(article)
          } yield AnalyzedArticle(tags, event.url, article.getTitle, article.getContent)
        }
    }

  private def extractTags(
      article: Article,
      keywordCount: Int = 10
  ): ZIO[Any, Throwable, List[String]] = {
    for {
      tags <- nlpProcessor.extractKeywords(article.getContent, keywordCount)
    } yield tags
  }
}

object ArticleIngestion {
  def live: ZLayer[ArticlesRepository, Nothing, ArticleIngestion] =
    ZLayer.fromFunction(new ArticleIngestion(new NLPProcessorImpl(), _))
}
