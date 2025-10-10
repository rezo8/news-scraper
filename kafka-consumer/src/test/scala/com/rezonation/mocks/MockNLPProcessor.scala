package com.rezonation.mocks

import zio._
import com.rezonation.services.NLPProcessor

class MockNLPProcessor extends NLPProcessor {
  override def extractKeywords(text: String, maxKeywords: Int): Task[List[String]] = {
    // Return a predefined result for testing
    ZIO.succeed(MockNLPProcessor.defaultKeywords)
  }
}

object MockNLPProcessor {
  val defaultKeywords = List("mocked keyword1", "mocked keyword2")
}
