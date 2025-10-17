package com.rezonation.services

import com.rezonation.mocks.MockNLPProcessor
import com.rezonation.types.events.ProcessArticleEvent
import zio.*
import zio.test.*
import zio.test.Assertion.*

object ArticleIngesterSpec extends ZIOSpecDefault {
  val nlpProcessor = new MockNLPProcessor()
  val ingester     = new ArticleIngestion(nlpProcessor)

  override def spec: Spec[Any, Throwable] = suite("ArticleIngesterSpec")(
    test("ingestArticles should process a list of ProcessArticleEvent") {
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
