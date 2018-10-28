package com.meetpraveen

//#quick-start-server
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.concurrent.duration.Duration
import scala.util.{ Failure, Success }

import com.meetpraveen.actor.CustomerRegistryActor
import com.meetpraveen.model.Constants.cassandraUrl
import com.meetpraveen.persistency.EmbeddedCassandra

import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives.handleExceptions
import akka.stream.ActorMaterializer

//#main-class
object QuickstartServer extends App with CustomerRoutes {

  // set up ActorSystem and other dependencies here
  //#main-class
  //#server-bootstrapping
  implicit val system: ActorSystem = ActorSystem("helloAkkaHttpServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher
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
