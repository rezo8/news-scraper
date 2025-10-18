package com.rezonation.services

import com.rezonation.utils.Logging
import zio._
import scala.util.matching.Regex
import scala.collection.mutable

trait NLPProcessor {
  def extractKeywords(text: String, maxKeywords: Int): Task[List[String]]
}

class NLPProcessorImpl extends NLPProcessor {
  private val stopWords           = Set(
    "a",
    "all",
    "an",
    "and",
    "are",
    "as",
    "at",
    "be",
    "been",
    "but",
    "by",
    "city",
    "el",
    "end",
    "for",
    "from",
    "has",
    "have",
    "in",
    "is",
    "it",
    "its",
    "last",
    "least",
    "not",
    "of",
    "on",
    "or",
    "people",
    "said",
    "that",
    "the",
    "these",
    "this",
    "those",
    "to",
    "was",
    "were",
    "will",
    "with"
  )
  // Only alphabetic tokens of length >= 2 (lowercase expected)
  private val tokenPattern: Regex = "[a-z]{2,}".r

  // TODO improve this
  override def extractKeywords(text: String, maxKeywords: Int): Task[List[String]] =
    Logging.timedLog("Completed Keyword Extraction")(for {
      tags <- ZIO.attemptBlocking {
                val normalized = Option(text).getOrElse("").toLowerCase

                val tokens = tokenPattern.findAllIn(normalized).toList.filterNot(stopWords.contains)

                if (tokens.isEmpty) List.empty
                else {
                  val unigrams = tokens
                  val bigrams  = tokens.sliding(2).map(_.mkString(" ")).toList
                  val trigrams = tokens.sliding(3).map(_.mkString(" ")).toList

                  val candidates = (unigrams ++ bigrams ++ trigrams)
                    .filter { phrase =>
                      val parts = phrase.split("\\s+")
                      parts.forall(p => p.forall(_.isLetter)) && phrase.length > 1
                    }

                  val freqMap = candidates.groupMapReduce(identity)(_ => 1)(_ + _)

                  val totalCandidates = candidates.size.toDouble
                  val firstIndex      = mutable.Map.empty[String, Int]
                  candidates.zipWithIndex.foreach { case (term, idx) =>
                    if (!firstIndex.contains(term)) firstIndex(term) = idx
                  }

                  val scores = freqMap.map { case (term, freq) =>
                    val tf            = freq / totalCandidates
                    // local idf: rarer within the doc -> higher value
                    val idf           = math.log((totalCandidates + 1.0) / (1.0 + freq))
                    val lengthBoost   = 1.0 + math.log(term.split("\\s+").length.toDouble)
                    // earlier occurrences get a bit of a boost
                    val positionBoost = 1.0 / (1.0 + firstIndex.getOrElse(term, Int.MaxValue / 2))
                    val combined      =
                      freq * 0.6 + (tf * idf) * 0.4 + lengthBoost * 0.1 + positionBoost * 0.05
                    term -> combined
                  }

                  val sorted = scores.toList
                    .sortBy { case (t, score) => (-score, t) }
                    .map(_._1)
                    .distinct
                    .take(maxKeywords)

                  // Remove terms that are substrings of other selected terms
                  val filtered =
                    sorted.filter(term => !sorted.exists(x => x != term && x.contains(term)))

                  // Track removed terms
                  val removed = sorted.diff(filtered)

                  // Refill only with terms not in filtered or removed
                  val refill = sorted
                    .filter(term => !filtered.contains(term) && !removed.contains(term))
                    .take(maxKeywords - filtered.size)

                  val result = filtered ++ refill

                  result
                }
              }
    } yield tags)
}

object NLPProcessor {
  val live: ZLayer[Any, Nothing, NLPProcessor] =
    ZLayer.succeed(new NLPProcessorImpl())
}
