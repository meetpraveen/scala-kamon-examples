package com.meetpraveen.hellostream.impl

import com.lightbend.lagom.scaladsl.akka.discovery.AkkaDiscoveryComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.server._
import com.meetpraveen.hello.api.HelloService
import com.meetpraveen.hellostream.api.HelloStreamService
import com.softwaremill.macwire._
import kamon.Kamon
import play.api.libs.ws.ahc.AhcWSComponents

class HelloStreamLoader extends LagomApplicationLoader {
  Kamon.init()
  override def load(context: LagomApplicationContext): LagomApplication =
    new HelloStreamApplication(context) with AkkaDiscoveryComponents

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new HelloStreamApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[HelloStreamService])
}

abstract class HelloStreamApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer: LagomServer = serverFor[HelloStreamService](wire[HelloStreamServiceImpl])

  // Bind the HelloService client
  lazy val helloService: HelloService = serviceClient.implement[HelloService]
}
