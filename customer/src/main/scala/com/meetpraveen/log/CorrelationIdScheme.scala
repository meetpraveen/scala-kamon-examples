package com.meetpraveen.log

import java.util.UUID

import kamon.Kamon
import kamon.trace.{Identifier, SpanBuilder, Tracer}
import kamon.context.Context

import scala.util.{Failure, Success, Try}

class CorrelationIdScheme extends Identifier.Scheme(UUIDIdentifier, Identifier.Factory.EightBytesIdentifier)

object CorrelationIdTracer {
  val Scheme = new CorrelationIdScheme
  val CorrId = "correlationId"
  val CorrContextId = Context.key(CorrId, "")
  val CorrelationIdHeader = "X-Correlation-Id"
  val Empty = UUIDIdentifier.generate()
}

object UUIDIdentifier extends Identifier.Factory {

  override def generate(): Identifier = {
    from(UUID.randomUUID())
  }

  private def from(id: UUID): Identifier = {
    val string = id.toString
    Identifier(string, string.getBytes("UTF-8"))
  }

  override def from(string: String): Identifier = {
    validateHeader(string)
  }

  override def from(bytes: Array[Byte]): Identifier = {
    from(new String(bytes, "UTF-8"))
  }

  private def validateHeader(string: String): Identifier = {
    Try(UUID.fromString(string)) match {
      case Success(value) => from(value)
      case Failure(_) => generate()
    }
  }
}

class FormatCorrId extends Tracer.PreStartHook {
  override def beforeStart(builder: SpanBuilder): Unit = {
    import kamon.context.Context
    val corrKeyId = Context.key("correlationId", "")
    val corrId: String = Kamon.currentContext().get(corrKeyId)
    val uuid = UUIDIdentifier.from(corrId)
    if (!uuid.string.equals(corrId)) {
      Kamon.storeContext(Kamon.currentContext().withEntry(corrKeyId, uuid.string))
    }
  }
}