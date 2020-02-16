package com.meetpraveen.metrics

import com.meetpraveen.log.LogContext
import kamon.Kamon
import kamon.metric.{ MeasurementUnit, Timer }
import kamon.tag.{ Tag, TagSet }

import scala.concurrent.Future
import scala.util.{ Failure, Success, Try }
import Monitoring.Constants._
import scala.language.implicitConversions

/**
 * All monitoring functions with encapsulation of kamon specific steps.
 *
 * @author psinha
 */
trait KamonMonitoring extends Monitoring {
  this: LogContext =>

  import KamonConstants.{ Exceptions, Implicits, TagSets }, Implicits._, TagSets._
  import scala.concurrent.ExecutionContext.Implicits.global

  override def timer[T](name: String, tags: Map[String, Any] = Map.empty)(f: => T): T = {
    val timer = Kamon.timer(name).withTags(tags)
    val timerStarted = timer.start()
    val t = Try(f)
    t match {
      case Success(`try`: Try[_]) => handleTry(`try`, name, timerStarted)
      case Success(future: Future[_]) => future.onComplete(handleTry(_, name, timerStarted))
      case `try`: Try[_] => handleTry(`try`, name, timerStarted)
    }
    t.get
  }

  private def handleTry[T]: PartialFunction[(Try[T], String, Timer.Started), Unit] = {
    case (Success(_), _, timerStarted) => timerStarted.withTags(SuccessTag).stop()
    case (Failure(exception), name, timerStarted) =>
      timerStarted.withTags(FailureTag).stop()
      Exceptions.handle(this)(name, exception)
  }

  override def counter(name: String, ops: CounterOps, unit: MeasurementUnit, tags: Map[String, Any] = Map.empty): Unit = {
    ops match {
      case IncrementCounterBy(x) => Kamon.counter(name, unit).withTags(TagSet.from(tags)).increment(x)
    }
  }

  override def gauge(name: String, ops: GaugeOps, tags: Map[String, Any] = Map.empty): Unit = {
    ops match {
      case IncrementGaugeOpsBy(x) => Kamon.gauge(name).withTags(TagSet.from(tags)).increment(x)
      case DecrementGaugeOpsBy(y) => Kamon.gauge(name).withTags(TagSet.from(tags)).decrement(y)
      case SetGaugeOps(z) => Kamon.gauge(name).withTags(TagSet.from(tags)).update(z)
    }
  }
}

object KamonConstants {

  object Exceptions {
    def handle(monitoring: Monitoring)(name: String, throwable: Throwable, tags: TagSet = TagSet.Empty): Unit = {
      monitoring.counter(s"$name$ExceptionHandledCounterSuffix", IncrementCounterBy(), MeasurementUnit.none, Map(ExceptionTagName -> throwable.getClass.getSimpleName))
    }

    def rethrown(monitoring: Monitoring)(name: String, throwable: Throwable, tags: TagSet = TagSet.Empty): Unit = {
      monitoring.counter(s"$name$ExceptionRethrownCounterSuffix", IncrementCounterBy(), MeasurementUnit.none, Map(ExceptionTagName -> throwable.getClass.getSimpleName))
    }
  }

  object TagSets {
    val StatusTagKey = "status"
    val FailureTag: TagSet = TagSet.of(StatusTagKey, "failure")
    val SuccessTag: TagSet = TagSet.of(StatusTagKey, "success")
  }

  object Implicits {
    implicit def toScalaMap(tagSet: TagSet): Map[String, Any] =
      tagSet.all().map(tag => tag.key -> Tag.unwrapValue(tag)).toMap

    implicit def toTagSet(map: Map[String, Any]): TagSet = TagSet.from(map)
  }

}

