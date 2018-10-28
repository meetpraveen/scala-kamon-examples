package com.meetpraveen.route

import scala.concurrent.Future
import scala.concurrent.duration._

import com.meetpraveen.actor.CustomerRegistryActor._
import com.meetpraveen.model.{ Customer, Customers, JsonSupport }

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{ delete, get, post }
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import scala.util.{ Try, Success, Failure }
import akka.http.scaladsl.server.ExceptionHandler
import scala.concurrent.ExecutionContext
import com.meetpraveen.LogContext
import com.meetpraveen.LogUtils._

//#customer-routes-class
trait CustomerRoutes extends JsonSupport {
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
      ex.printStackTrace
      complete(StatusCodes.BadRequest, ex.getMessage)
    }
  }
  //#all-routes
  //#customers-get-post
  //#customers-get-delete
  lazy val customerRoutes: Route =
    pathPrefix("customers") {
      //#customers-get-delete
      pathEndOrSingleSlash {
        get {
          val customers = (customerRegistryActor ? GetCustomers).mapTo[Try[Customers]]
          complete(customers)
        } ~
          post {
            entity(as[Customer]) { customer =>
              val customerCreated =
                (customerRegistryActor ? CreateCustomer(customer)).mapTo[Try[Customer]]
              onSuccess(customerCreated) { performed =>
                log.info("Created customer [{}]", performed)
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
            val maybecustomer =
              (customerRegistryActor ? GetCustomer(id)).mapTo[Try[Option[Customer]]]
            onSuccess(maybecustomer) { customer =>
              rejectEmptyResponse {
                complete(maybecustomer)
              }
            }
            //#retrieve-customer-info
          } ~
            delete {
              //#customers-delete-logic
              val customerDeleted =
                (customerRegistryActor ? DeleteCustomer(id)).mapTo[Try[Unit]]
              onSuccess(customerDeleted) { performed =>
                log"Deleted customer [${id.toString}]"
                complete(StatusCodes.OK, "Deleted")
              }
              //#customers-delete-logic
            }
        }
      //#customers-get-delete
    }
  //#all-routes
}
