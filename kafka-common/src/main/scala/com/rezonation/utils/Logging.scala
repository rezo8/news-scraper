package com.rezonation.utils

import zio.*

object Logging {
  def timedLog[R, E, A](logMessage: String)(zio: ZIO[R, E, A]): ZIO[R, E, A] =
    for {
      start   <- Clock.nanoTime
      result  <- zio
      end     <- Clock.nanoTime
      duration = (end - start).nanos
      _       <- ZIO.logAnnotate("duration_ms", duration.toMillis.toString)(ZIO.log(logMessage))
    } yield result
}
