package com.meetpraveen.log

import com.typesafe.config.{ Config, ConfigFactory }
import kamon.ClassLoading
import kamon.context.HttpPropagation.{ HeaderReader, HeaderWriter }
import kamon.context.{ Context, Propagation }

import scala.util.Try

class CorrelationIdHeaderReader extends Propagation.EntryReader[HeaderReader] {
  lazy val httpDefaultConfigPath = "kamon.instrumentation.http-server.default"
  lazy val httpDefaultConfig = config.getConfig(httpDefaultConfigPath)
  lazy val config: Config = Try(ConfigFactory.load(ClassLoading.classLoader())).recoverWith {
    case _ => Try(ConfigFactory.defaultReference(ClassLoading.classLoader()))
  }.fold(_ => ConfigFactory.empty(), identity)

  override def read(medium: HeaderReader, context: Context): Context = {
    val identifierScheme = CorrelationIdTracer.Scheme
    val corrIdHeader: String = Option(httpDefaultConfig.getString("tracing.response-headers.trace-id"))
      .filter(!_.isEmpty)
      .getOrElse(CorrelationIdTracer.CorrelationIdHeader)
    val traceID = medium.read(corrIdHeader)
      .map(identifierScheme.traceIdFactory.from)
      .getOrElse(CorrelationIdTracer.Empty)

    context.withTag(CorrelationIdTracer.CorrId, traceID.string)
  }
}
