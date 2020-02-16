package com.meetpraveen.metrics

import kamon.metric.MeasurementUnit
import kamon.tag.{ Tag, TagSet }

/**
 * Base monitoring trait, exposes basic measurement functions
 * @author psinha
 */
trait Monitoring {
  def timer[T](name: String, tags: Map[String, Any] = Map.empty)(f: => T): T
  def gauge(name: String, ops: GaugeOps, tags: Map[String, Any] = Map.empty)
  def counter(name: String, ops: CounterOps, unit: MeasurementUnit, tags: Map[String, Any] = Map.empty)
}

/**
 * Companion Oobject
 */
object Monitoring {
  object Constants {
    val ExceptionHandledCounterSuffix = ".exceptions.handled"
    val ExceptionRethrownCounterSuffix = ".exceptions.rethrown"
    val ExceptionTagName = "exception"
  }
}

/**
 * Counter operation types
 */
sealed trait CounterOps
case class IncrementCounterBy(incrementBy: Int = 1) extends CounterOps

/**
 * Gauge operation types
 */
sealed trait GaugeOps
case class IncrementGaugeOpsBy(incrementBy: Int = 1) extends GaugeOps
case class DecrementGaugeOpsBy(incrementBy: Int = 1) extends GaugeOps
case class SetGaugeOps(value: Double) extends GaugeOps