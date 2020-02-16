package com.meetpraveen.directives

import java.util.UUID

import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directive
import akka.http.scaladsl.server.Directives._
import org.slf4j.MDC

trait TrackingDirectives {

  val CorrelationIdKey = "corrId"
  val `X-Correlation-Header` = "X-Corr-Header"

  def extractCorrelationId: Directive[Unit] = optionalHeaderValueByName(`X-Correlation-Header`).map { correlationId =>
    val corr = correlationId.getOrElse(UUID.randomUUID().toString)
    MDC.put(CorrelationIdKey, corr)
  }

  def repondWithCorrelationHeader = mapResponseHeaders { headers =>
    val cor = Option(MDC.get(CorrelationIdKey)).getOrElse("bad")
    val newHeaders = headers :+ RawHeader(`X-Correlation-Header`, cor)
    MDC.remove(CorrelationIdKey)
    newHeaders
  }
}
