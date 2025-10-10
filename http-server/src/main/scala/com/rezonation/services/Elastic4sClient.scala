package com.rezonation.services

import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.ElasticProperties
import com.sksamuel.elastic4s.http.JavaClient
import zio._

object Elastic4sClient {

  def createClient: ZIO[Any, Throwable, ElasticClient] =
    ZIO.attempt {
      ElasticClient(
        JavaClient(ElasticProperties("http://localhost:9200"))
      )
    }

  def closeClient(client: ElasticClient): ZIO[Any, Nothing, Unit] =
    ZIO.attempt(client.close()).orDie
}
