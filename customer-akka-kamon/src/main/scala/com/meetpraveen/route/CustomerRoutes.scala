package com.meetpraveen.route

// Exposes mdc aware send and ask to actor
import akka.MDCAwareActor.Implicits._
import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.MethodDirectives.{ delete, get, post }
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.server.{ ExceptionHandler, Route }
import akka.util.Timeout
import com.meetpraveen.actor.CustomerRegistryActor._
import com.meetpraveen.directives.TrackingDirectives
import kamon.Kamon
import kamon.context.Context
import org.slf4j.MDC

import scala.xml.Elem

// Exposes log implicits for string interpolation, future withLogging etc.
import com.meetpraveen.log.LogUtils._

// Exposes implicit logger
import com.meetpraveen.log.LogContext
import com.meetpraveen.model.{ Customer, Customers, JsonSupport }

import scala.concurrent.duration._
import scala.util.Try

//#customer-routes-class
trait CustomerRoutes extends JsonSupport with TrackingDirectives {
  self: LogContext =>
  //#customer-routes-class

  // we leave these abstract, since they will be provided by the App
  implicit def system: ActorSystem

  // other dependencies that customerRoutes use
  def customerRegistryActor: ActorRef

  // Required by the `ask` (?) method below
  implicit lazy val timeout = Timeout(15.seconds) // usually we'd obtain the timeout from the system's configuration

  implicit val exceptionHandler = ExceptionHandler {
    case ex: Throwable => {
      log.error("Execption in route", ex)
      complete(StatusCodes.BadRequest, ex.getMessage)
    }
  }
  //#all-routes
  //#customers-get-post
  //#customers-get-delete
  lazy val customerRoutes: Route =
    // Extract correlation id from header and pass it to MDC, finally send it back as response header
    //    (extractCorrelationId & repondWithCorrelationHeader) {
    pathPrefix("customers") {
      //#customers-get-delete
      pathEndOrSingleSlash {
        get {
          val xml: Elem = <start>hello</start>
          val customers = (customerRegistryActor ?? GetCustomers).mapTo[Try[Customers]].withLogAndTags(Map("name" -> xml))("Route: Get customers")
          complete(customers)
        } ~
          post {
            entity(as[Customer]) { customer =>
              val customerCreated =
                (customerRegistryActor ?? CreateCustomer(customer)).mapTo[Try[Customer]].withLogging("Route Created customer")
              onSuccess(customerCreated) { performed =>
                complete(StatusCodes.Created, performed)
              }
            }
          }
      } ~
        //#customers-get-post
        //#customers-get-delete
        path(JavaUUID) { id =>
          get {
            //#retrieve-customer-info
            val context = Kamon.currentContext().withEntry(Context.key("poison", "undefined"), "available")
            Kamon.runWithContext(context) {
              val maybecustomer =
                (customerRegistryActor ?? GetCustomer(id)).mapTo[Try[Option[Customer]]].withLogAndEntries(Map(Context.key("mySpecialContext", "get/cusotmer/:id") -> "available"))("Route Get customer(id)")
              onSuccess(maybecustomer) { customer =>
                rejectEmptyResponse {
                  complete(maybecustomer)
                }
              }
            }
            //#retrieve-customer-info
          } ~
            delete {
              //#customers-delete-logic
              val customerDeleted =
                (customerRegistryActor ?? DeleteCustomer(id)).mapTo[Try[Unit]]
              onSuccess(customerDeleted) { performed =>
                info"Deleted customer [${id.toString}]"
                complete(StatusCodes.OK, "Deleted")
              }
              //#customers-delete-logic
            }
        }
      //#customers-get-delete
    }
  //}
  //#all-routes
}
