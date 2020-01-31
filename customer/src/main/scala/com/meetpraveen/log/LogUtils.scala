package com.meetpraveen.log

import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.Future
import scala.util.{Failure, Success}

object LogUtils {
  // Support for logging interpolated strings
  implicit class LogEnhancer[T](val logStr: StringContext) extends AnyVal {
    def warn(args: Any*)(implicit log: Logger): Unit = {
      log.warn(logStr.raw(args: _*))
    }
    def info(args: Any*)(implicit log: Logger): Unit = {
      log.info(logStr.raw(args: _*))
    }
    def debug(args: Any*)(implicit log: Logger): Unit = {
      log.debug(logStr.raw(args: _*))
    }
    def error(ex: Throwable, args: Any*)(implicit log: Logger): Unit = {
      log.error(logStr.raw(args: _*), ex)
    }
    def error(args: Any*)(implicit log: Logger): Unit = {
      log.error(logStr.raw(args: _*))
    }
  }

  import com.meetpraveen.mdcaware.MDCPropagatingExecutionContext.Implicits.global

  implicit class LogFuture[T](val f: Future[T]) {
    def withLogging(message: String, logTypeExtractor: T => String = _.toString)(implicit log: Logger): Future[T] = {
      f.onComplete{
        case Success(value) => log.debug(s"$message :: Success ${logTypeExtractor(value)}")
        case Failure(exception) => log.error(s"$message :: Failure", exception)
      }
      f
    }
  }
}

case class CorrelationWrapper(correlationId: String)

// Simple trait for mixing in logger
trait LogContext {
  implicit val log: Logger = LoggerFactory.getLogger(this.getClass)
}
