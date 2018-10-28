package com.meetpraveen

import org.slf4j.LoggerFactory
import org.slf4j.Logger
import scala.concurrent.Future

object LogUtils {
  // Support for logging interpolated strings
  implicit class LogEnhancer[T](val logStr: StringContext) extends AnyVal {
    def log(args: String*)(implicit log: Logger) = {
      logStr.raw(args: _*)
      log.debug(logStr.raw(args: _*))
    }
  }
}

// Simple trait for mixing in logger
trait LogContext {
  implicit val log = LoggerFactory.getLogger(this.getClass)
}