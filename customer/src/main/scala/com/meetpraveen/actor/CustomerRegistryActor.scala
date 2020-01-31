package com.meetpraveen.actor

//#customer-registry-actor
import java.util.UUID

import akka.MDCAwareActor
import akka.actor.{Props, actorRef2Scala}
import com.meetpraveen.log.LogUtils._
import com.meetpraveen.mdcaware.MDCPropagatingExecutionContextWrapper
import com.meetpraveen.model.Customer
import com.meetpraveen.persistency.CassandraPersistency

import scala.concurrent.ExecutionContext

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

class CustomerRegistryActor extends MDCAwareActor with CassandraPersistency {
  import CustomerRegistryActor._

  implicit val ex: ExecutionContext = MDCPropagatingExecutionContextWrapper(context.dispatcher)
  def receive: Receive = {
    case msg @ GetCustomers => {
      val sndr = sender()
      debug"Actor: CustomerRegistryActor Received $msg"
      getCustomers().onComplete(sndr ! _)
    }
    case msg @ CreateCustomer(customer) => {
      val sndr = sender()
      debug"Actor: CustomerRegistryActor Received $msg"
      upsertCustomer(customer).onComplete(sndr ! _)
    }
    case msg @ UpdateCustomer(customer) => {
      val sndr = sender()
      debug"Actor: CustomerRegistryActor Received $msg"
      upsertCustomer(customer).onComplete(sndr ! _)
    }
    case msg @ GetCustomer(id) => {
      val sndr = sender()
      debug"Actor: CustomerRegistryActor Received $msg"
      getCustomer(id).onComplete(sndr ! _)
    }
    case msg @ DeleteCustomer(id) =>
      val sndr = sender()
      debug"Actor: CustomerRegistryActor Received $msg"
      deleteCustomer(id).onComplete(sndr ! _)
  }
}
//#customer-registry-actor