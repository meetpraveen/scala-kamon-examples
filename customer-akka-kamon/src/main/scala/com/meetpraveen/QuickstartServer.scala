package com.meetpraveen

//#quick-start-server
import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.handleExceptions
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.meetpraveen.actor.CustomerRegistryActor
import com.meetpraveen.directives.TrackingDirectives
import com.meetpraveen.log.LogContext
import com.meetpraveen.mdcaware.MDCPropagatingExecutionContextWrapper
import com.meetpraveen.model.Constants.cassandraUrl
import com.meetpraveen.persistency.EmbeddedCassandra
import com.meetpraveen.route.CustomerRoutes

import kamon.Kamon

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.util.{ Failure, Success }

//#main-class
object QuickstartServer extends App with CustomerRoutes with TrackingDirectives with LogContext {
  Kamon.init()
  // set up ActorSystem and other dependencies here
  //#main-class
  //#server-bootstrapping
  implicit val system: ActorSystem = ActorSystem("helloAkkaHttpServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher //MDCPropagatingExecutionContextWrapper(system.dispatcher)
  //#server-bootstrapping

  // Starting up embedded cassandra
  if (cassandraUrl == "localhost") EmbeddedCassandra.init()

  // Create worker actor
  val customerRegistryActor: ActorRef = system.actorOf(CustomerRegistryActor.props, "customerRegistryActor")

  //#main-class
  // from the customerRoutes trait
  lazy val routes: Route = handleExceptions(exceptionHandler) { customerRoutes }
  //#main-class

  //#http-server
  val serverBinding: Future[Http.ServerBinding] = Http().bindAndHandle(routes, "localhost", 8080)

  serverBinding.onComplete {
    case Success(bound) =>
      println(s"Server online at http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/")
    case Failure(e) =>
      Console.err.println(s"Server could not start!")
      e.printStackTrace()
      system.terminate()
  }

  Await.result(system.whenTerminated, Duration.Inf)
  //#http-server
  //#main-class
}
//#main-class
//#quick-start-server
