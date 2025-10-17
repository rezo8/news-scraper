package com.rezonation.services

import zio._
import zio.test._
import zio.test.Assertion._
import com.rezonation.types.events.ProcessArticleEvent
import com.rezonation.mocks.MockNLPProcessor
import net.dankito.readability4j.Article

object ArticleIngesterSpec extends ZIOSpecDefault {
  val nlpProcessor = new MockNLPProcessor()
  val ingester     = new ArticleAnalyzer(nlpProcessor)

  override def spec = suite("ArticleIngesterSpec")(
    test("ingestArticles should process a list of ProcessArticleEvent") {
      // Arrange: Create a mock list of ProcessArticleEvent
      val mockEvents = List(
        // ProcessArticleEvent("https://example.com/article1"),
        ProcessArticleEvent(
          "https://edition.cnn.com/2025/10/01/middleeast/analysis-trump-israel-gaza-peace-plan-latam-intl"
        )
      )

      val result = ingester.ingestArticles(mockEvents)

      assertZIO(result)(isNonEmpty)
    }
  )
}
