package com.rezonation.logging

import zio.*
import zio.http.*

object LoggingMiddleware {

  private val correlationIdMiddleware =
    Middleware.logAnnotate { req =>
      val correlationId =
        req.headers
          .get("X-Correlation-ID")
          .getOrElse(
            Unsafe.unsafe { implicit unsafe =>
              Runtime.default.unsafe.run(Random.nextUUID.map(_.toString)).getOrThrow()
            }
          )
      val requestId     =
        req.headers
          .get("X-Request-ID")
          .getOrElse(
            Unsafe.unsafe { implicit unsafe =>
              Runtime.default.unsafe.run(Random.nextUUID.map(_.toString)).getOrThrow()
            }
          )

      Set(LogAnnotation("correlation_id", correlationId), LogAnnotation("request_id", requestId))
    }

  val defaultMiddlewares: Middleware[Any] =
    correlationIdMiddleware ++ Middleware.requestLogging() ++
      Middleware.timeout(5.seconds)

}
