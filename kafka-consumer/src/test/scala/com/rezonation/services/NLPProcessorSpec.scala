package com.rezonation.services

import zio._
import zio.test._
import zio.test.Assertion._

object NLPProcessorSpec extends ZIOSpecDefault {

  override def spec = suite("NLPProcessorSpec")(
    test("extractKeywords should extract meaningful tokens") {
      val text = "Scala is a great programming language for building scalable systems."
      for {
        nlpProcessor <- ZIO.service[NLPProcessor]
        result       <- nlpProcessor.extractKeywords(text, 5)
      } yield assertTrue(
        result.nonEmpty &&
          result.exists(_.contains("scala")) &&
          result.exists(_.contains("programming"))
      )
    }.provideLayer(NLPProcessor.live),
    test("extractKeywords should exclude common stop words") {
      val text = "The cat is on the mat and the dog is in the house."
      for {
        nlpProcessor <- ZIO.service[NLPProcessor]
        result       <- nlpProcessor.extractKeywords(text, 5)
      } yield assertTrue(
        result.forall(!_.contains("the")) &&
          result.forall(!_.contains("is")) &&
          result.exists(_.contains("cat"))
      )
    }.provideLayer(NLPProcessor.live),
    test("extractKeywords should respect maxKeywords limit") {
      val text        = "Scala is great. ZIO is great. Functional programming is great."
      val maxKeywords = 3
      for {
        nlpProcessor <- ZIO.service[NLPProcessor]
        result       <- nlpProcessor.extractKeywords(text, maxKeywords)
      } yield assertTrue(result.length <= maxKeywords)
    }.provideLayer(NLPProcessor.live),
    test("extractKeywords should return empty for empty input") {
      val text = ""
      for {
        nlpProcessor <- ZIO.service[NLPProcessor]
        result       <- nlpProcessor.extractKeywords(text, 5)
      } yield assertTrue(result.isEmpty)
    }.provideLayer(NLPProcessor.live),
    test("extractKeywords should return the word itself for single-token input") {
      val text = "Scala"
      for {
        nlpProcessor <- ZIO.service[NLPProcessor]
        result       <- nlpProcessor.extractKeywords(text, 5)
      } yield assertTrue(result == List("scala"))
    }.provideLayer(NLPProcessor.live),
    test("extractKeywords should not produce duplicates") {
      val text = "Scala Scala Scala is great."
      for {
        nlpProcessor <- ZIO.service[NLPProcessor]
        result       <- nlpProcessor.extractKeywords(text, 5)
      } yield assertTrue(result.distinct == result)
    }.provideLayer(NLPProcessor.live),
    test("extractKeywords should ignore special characters") {
      val text = "Scala! Scala? Scala, is great!!!"
      for {
        nlpProcessor <- ZIO.service[NLPProcessor]
        result       <- nlpProcessor.extractKeywords(text, 5)
      } yield assertTrue(
        result.exists(_.contains("scala")) &&
          result.forall(!_.exists(c => "!?,.".contains(c)))
      )
    }.provideLayer(NLPProcessor.live),
    test("extractKeywords should handle numeric input gracefully") {
      val text = "123 456 789 Scala!"
      for {
        nlpProcessor <- ZIO.service[NLPProcessor]
        result       <- nlpProcessor.extractKeywords(text, 5)
      } yield assertTrue(result == List("scala"))
    }.provideLayer(NLPProcessor.live),
    test("extractKeywords should work correctly under concurrent calls") {
      val text  = "Scala is a great programming language."
      val tasks = ZIO.collectAllPar(
        List.fill(10)(ZIO.serviceWithZIO[NLPProcessor](_.extractKeywords(text, 3)))
      )
      for {
        results <- tasks
      } yield assertTrue(results.forall(_.nonEmpty))
    }.provideLayer(NLPProcessor.live)
  )
}
