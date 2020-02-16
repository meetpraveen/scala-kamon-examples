package com.meetpraveen.model

import scala.util.Properties.envOrElse

object Constants {
  val cassandraUrl = envOrElse("cassandraUrl", "localhost")
  val cassandraPort = envOrElse("cassandraPort", "9142")
}