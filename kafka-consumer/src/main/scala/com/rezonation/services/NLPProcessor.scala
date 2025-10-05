package com.rezonation.services

import zio._
import scala.util.matching.Regex

trait NLPProcessor {
  def extractKeywords(text: String, maxKeywords: Int): Task[List[String]]
}

class NLPProcessorImpl extends NLPProcessor {
  private val stopWords = Set(
    "the",
    "a",
    "an",
    "and",
    "or",
    "but",
    "is",
    "are",
    "was",
    "were",
    "in",
    "on",
    "at",
    "of",
    "for",
    "to",
    "with",
    "from"
  )

  def extractKeywords(text: String, maxKeywords: Int): Task[List[String]] =
    ZIO.attemptBlocking {
      // 1. Tokenize words
      val wordPattern: Regex = "\\b\\w+\\b".r
      val tokens             = wordPattern.findAllIn(text.toLowerCase).toList.filterNot(stopWords.contains)

      // 2. Build candidate n-grams (1-3 words)
      val unigrams   = tokens
      val bigrams    = tokens.sliding(2).map(_.mkString(" ")).toList
      val trigrams   = tokens.sliding(3).map(_.mkString(" ")).toList
      val candidates = (unigrams ++ bigrams ++ trigrams).filter(_.length > 1)

      // 3. Frequency scoring
      val freqMap = candidates.groupMapReduce(identity)(_ => 1)(_ + _)

      // 4. Pick top N
      freqMap.toList.sortBy(-_._2).take(maxKeywords).map(_._1)
    }
}

object NLPProcessor {
  val live: ZLayer[Any, Nothing, NLPProcessor] =
    ZLayer.succeed(new NLPProcessorImpl())

  // TODO implement these convenience methods

  // def extractKeywords(
  //     text: String,
  //     maxKeywords: Int = 10
  // ): ZIO[NLPProcessor, Throwable, List[String]] =
  //   ZIO.serviceWithZIO[NLPProcessorImpl](_.extractKeywords(text, maxKeywords))
}
