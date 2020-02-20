package com.meetpraveen.metrics

import java.time.Duration

import com.meetpraveen.log.LogContext
import com.typesafe.config.Config
import kamon.metric.PeriodSnapshot
import kamon.module.{MetricReporter, Module, ModuleFactory}
import com.meetpraveen.log.LogUtils._

class MyReporter(configPath: String) extends MetricReporter with LogContext{
  val accumulator: PeriodSnapshot.Accumulator = PeriodSnapshot.accumulator(Duration.ofSeconds(60), Duration.ofSeconds(1))

  override def reportPeriodSnapshot(snapshot: PeriodSnapshot): Unit = {
    accumulator.add(snapshot).foreach { snapshot =>
      val counters = snapshot.counters
      val gauges = snapshot.gauges
      val histograms = snapshot.histograms
      val timers = snapshot.timers
      val rangeSamplers = snapshot.rangeSamplers
      val countString = counters.map(met => s"${met.name} - [${met.instruments.mkString(",")}]").mkString(";").take(200)
      val gaugeString = gauges.map(met => s"${met.name} - [${met.instruments.mkString(",")}]").mkString(";").take(200)
      val histString = histograms.map(met => s"${met.name} - [${met.instruments.mkString(",")}]").mkString(";").take(200)
      val timersString = timers.map(met => s"${met.name} - [${met.instruments.mkString(",")}]").mkString(";").take(200)
      val rangeString = rangeSamplers.map(met => s"${met.name} - [${met.instruments.mkString(",")}]").mkString(";").take(200)
      log.info(
        s"""
           |==================================================================================================
           |Count     $countString
           |Gauge     $gaugeString
           |Timer     $timersString
           |Histogram $histString
           |Range     $rangeString
           |==================================================================================================
           |""".stripMargin)
    }
  }

  override def stop(): Unit = {
    // Disconnect client
  }

  override def reconfigure(newConfig: Config): Unit = {
    // Reset client if client conf changes
  }
}

object MyReporter {
  class Factory extends ModuleFactory {
    override def create(settings: ModuleFactory.Settings): Module =
      new MyReporter("kamon.myreporter")
  }
}

case class MyReportConfig()
object MyReportConfig {
  def apply(myReportConfig: Config) = new MyReportConfig()
}
