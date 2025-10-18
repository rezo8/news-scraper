package com.rezonation.types.events

import java.util.UUID
import zio.ZIO

trait BaseEvent[+A <: BaseEvent[A]] {
  def correlationId: String
  def applyCorrelationId(correlationId: String): A
}

object BaseEvent {
  def constructEvent[A <: BaseEvent[A]](constructor: () => A): ZIO[Any, Nothing, A] = {
    for {
      correlationId <- ZIO.logAnnotations.map(_.get("correlation_id"))
    } yield constructor().applyCorrelationId(
      correlationId.getOrElse(UUID.randomUUID().toString)
    )
  }
}
