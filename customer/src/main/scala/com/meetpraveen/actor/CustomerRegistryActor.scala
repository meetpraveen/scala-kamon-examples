package com.meetpraveen.actor

//#customer-registry-actor
import akka.actor.{ Actor, ActorLogging, Props }
import akka.actor.actorRef2Scala
import com.meetpraveen.model.Customer
import com.meetpraveen.model.Customers
import com.meetpraveen.persistency.CassandraPersistency
import java.util.UUID
import scala.concurrent.ExecutionContext
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.util.Try

object CustomerRegistryActor {
  final case class InitCassandraSession(cassandraUrl: String, cassandraPort: String)
  final case class ActionPerformed(description: String)
  final case object GetCustomers
  final case class CreateCustomer(customer: Customer)
  final case class UpdateCustomer(customer: Customer)
  final case class GetCustomer(id: UUID)
  final case class DeleteCustomer(id: UUID)

  def props: Props = Props[CustomerRegistryActor]
}

class CustomerRegistryActor extends Actor with ActorLogging with CassandraPersistency {
  import CustomerRegistryActor._

  implicit val ex: ExecutionContext = context.dispatcher
  def receive: Receive = {
    case InitCassandraSession(url, port) => sender()
    case GetCustomers => {
      val sndr = sender()
      sndr ! Try(Await.result(getCustomers(), 10 seconds))
    }
    case CreateCustomer(customer) => {
      val sndr = sender()
      sndr ! Try(Await.result(upsertCustomer(customer), 10 seconds))
    }
    case UpdateCustomer(customer) => {
      val sndr = sender()
      sndr ! Try(Await.result(upsertCustomer(customer), 10 seconds))
    }
    case GetCustomer(id) => {
      val sndr = sender()
      sndr ! Try(Await.result(getCustomer(id), 10 seconds))
    }
    case DeleteCustomer(id) =>
      val sndr = sender()
      sndr ! Try(Await.result(deleteCustomer(id), 10 seconds))
  }
}
//#customer-registry-actor