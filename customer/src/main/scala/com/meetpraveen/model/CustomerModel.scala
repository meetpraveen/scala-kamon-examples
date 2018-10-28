package com.meetpraveen.model

import com.meetpraveen.actor.CustomerRegistryActor.ActionPerformed
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import java.util.UUID
import spray.json.JsValue
import spray.json.RootJsonFormat
import spray.json.JsString

//#customer-case-classes
trait Identifiable {
  def id: UUID
  def identity: UUID = {
    id
  }
}
final case class Customer(id: UUID = UUID.randomUUID(), name: String = "test", age: Int = 1, countryOfResidence: String = "India") extends Identifiable
final case class Customers(customers: Seq[Customer])
//#customer-case-classes

trait JsonSupport extends SprayJsonSupport {
  // import the default encoders for primitive types (Int, String, Lists etc)
  import spray.json.DefaultJsonProtocol._
  import spray.json.deserializationError

  // provide specific formatter for complex objects like UUID
  implicit object UuidJsonFormat extends RootJsonFormat[UUID] {
    def write(x: UUID) = JsString(x.toString)
    def read(value: JsValue) = value match {
      case JsString(x) => UUID.fromString(x)
      case x => deserializationError(s"Expected UUID as JsString, but got {x}")
    }
  }

  implicit val customerJsonFormat = jsonFormat4(Customer)
  implicit val customersJsonFormat = jsonFormat1(Customers)

  implicit val actionPerformedJsonFormat = jsonFormat1(ActionPerformed)
}
//#json-support
