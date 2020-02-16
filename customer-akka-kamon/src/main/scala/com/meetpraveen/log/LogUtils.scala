package com.meetpraveen.log

import kamon.Kamon
import kamon.context.Context
import kamon.tag.TagSet
import org.slf4j.{ Logger, LoggerFactory }

import scala.concurrent.Future
import scala.util.{ Failure, Success, Try }

object LogUtils {
  type Entries[T] = Map[Context.Key[T], T]
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
      f.onComplete(_.withLogging(message))
      f
    }
    def withLogAndTags(tags: Map[String, Any])(message: String, logTypeExtractor: T => String = _.toString)(implicit log: Logger): Future[T] = {
      f.onComplete(_.withLogAndTags(tags)(message))
      f
    }
    def withLogAndEntries(entries: Entries[String])(message: String, logTypeExtractor: T => String = _.toString)(implicit log: Logger): Future[T] = {
      f.onComplete(_.withLogAndEntries(entries)(message))
      f
    }
  }

  implicit class LogTry[T](val `try`: Try[T]) {
    def withLogging(message: String, logTypeExtractor: T => String = _.toString)(implicit log: Logger): Try[T] = {
      `try` match {
        case Success(value) => log.debug(s"$message :: Success ${logTypeExtractor(value)}")
        case Failure(exception) => log.error(s"$message :: Failure", exception)
      }
      `try`
    }
    def withLogAndTags(tags: Map[String, Any])(message: String, logTypeExtractor: T => String = _.toString)(implicit log: Logger): Try[T] = {
      `try` match {
        case Success(value) => logWithTags(TagSet.from(tags), s"$message :: Success ${logTypeExtractor(value)}")
        case Failure(ex) => logWithTagsAndThrowable(TagSet.from(tags), s"$message :: Failure ${ex.getMessage}", ex)
      }
      `try`
    }
    def withLogAndEntries(entries: Entries[String])(message: String, logTypeExtractor: T => String = _.toString)(implicit log: Logger): Try[T] = {
      `try` match {
        case Success(value) => logWithEntries(entries, s"$message :: Success ${logTypeExtractor(value)}")
        case Failure(ex) => logWithEntriesAndThrowable(entries, s"$message :: Failure ${ex.getMessage}", ex)
      }
      `try`
    }
  }

  def logWithTags(tags: TagSet, message: String)(implicit log: Logger): Unit = logWithTags(log.debug, tags, message)
  def logWithTagsAndThrowable(tags: TagSet, message: String, ex: Throwable)(implicit log: Logger): Unit = logWithTags(log.debug, tags, message, ex)

  def logWithTags(logFunction: String => Unit, tags: TagSet, message: String)(implicit log: Logger): Unit = {
    val context = Kamon.currentContext().withTags(tags)
    Kamon.runWithContext(context) {
      logFunction(message)
    }
  }

  def logWithTags(logFunction: (String, Throwable) => Unit, tags: TagSet, messag: String, ex: Throwable)(implicit log: Logger): Unit = {
    val context = Kamon.currentContext().withTags(tags)
    Kamon.runWithContext(context) {
      logFunction(messag, ex)
    }
  }

  def logWithEntries(entries: Entries[String], message: String)(implicit log: Logger): Unit = logWithEntries(log.debug, entries, message)
  def logWithEntriesAndThrowable(entries: Entries[String], message: String, ex: Throwable)(implicit log: Logger): Unit = logWithEntries(log.debug, entries, message, ex)

  def logWithEntries(logFunction: String => Unit, entries: Entries[String], message: String)(implicit log: Logger): Unit = {
    val context = entries.foldLeft(Kamon.currentContext())((agg, entry) => agg.withEntry(entry._1, entry._2))
    Kamon.runWithContext(context) {
      logFunction(message)
    }
  }

  def logWithEntries(logFunction: (String, Throwable) => Unit, entries: Entries[String], messag: String, ex: Throwable)(implicit log: Logger): Unit = {
    val context = entries.foldLeft(Kamon.currentContext())((agg, entry) => agg.withEntry(entry._1, entry._2))
    Kamon.runWithContext(context) {
      logFunction(messag, ex)
    }
  }
}

case class CorrelationWrapper(correlationId: String)

// Simple trait for mixing in logger
trait LogContext {
  implicit val log: Logger = LoggerFactory.getLogger(this.getClass)
}
