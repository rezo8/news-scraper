package com.rezonation.types.database

final case class AnalyzedArticle(
    tags: List[String],
    url: String,
    title: String,
    articleText: String
)
