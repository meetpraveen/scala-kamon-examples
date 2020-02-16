package com.meetpraveen.hellostream.impl

import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.meetpraveen.hellostream.api.HelloStreamService
import com.meetpraveen.hello.api.HelloService

import scala.concurrent.Future

/**
  * Implementation of the HelloStreamService.
  */
class HelloStreamServiceImpl(helloService: HelloService) extends HelloStreamService {
  def stream = ServiceCall { hellos =>
    Future.successful(hellos.mapAsync(8)(helloService.hello(_).invoke()))
  }
}
