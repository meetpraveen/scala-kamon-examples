package com.meetpraveen.directives

import java.util.UUID

import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directive
import akka.http.scaladsl.server.Directives._
import org.slf4j.MDC

trait TrackingDirectives {

  val CorrelationIdKey = "correlationId"
  val `X-Correlation-Header` = "X-Correlation-Header"

  def extractCorrelationId: Directive[Unit] = optionalHeaderValueByName(`X-Correlation-Header`).map { correlationId =>
    val corr = correlationId.getOrElse(UUID.randomUUID().toString)
    MDC.put(CorrelationIdKey, correlationId.getOrElse(UUID.randomUUID().toString))
  }

  def repondWithCorrelationHeader = mapResponseHeaders{ headers =>
    val newHeaders = headers :+ RawHeader(`X-Correlation-Header`, MDC.get(CorrelationIdKey))
    MDC.remove(CorrelationIdKey)
    newHeaders
  }
}
