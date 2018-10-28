package com.meetpraveen

//#customer-routes-spec
//#test-top
import akka.actor.ActorRef
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ Matchers, WordSpec }
import com.meetpraveen.actor.CustomerRegistryActor
import com.meetpraveen.model.Customer
import java.util.UUID

//#set-up
class customerRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest
    with CustomerRoutes {
  //#test-top

  // Here we need to implement all the abstract members of customerRoutes.
  // We use the real customerRegistryActor to test it while we hit the Routes, 
  // but we could "mock" it by implementing it in-place or by using a TestProbe() 
  override val customerRegistryActor: ActorRef =
    system.actorOf(CustomerRegistryActor.props, "customerRegistry")

  lazy val routes = customerRoutes

  //#set-up

  //#actual-test
  "customerRoutes" should {
    "return no customers if no present (GET /customers)" in {
      // note that there's no need for the host part in the uri:
      val request = HttpRequest(uri = "/customers")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        // and no entries should be in the list:
        entityAs[String] should ===("""{"customers":[]}""")
      }
    }
    //#actual-test

    //#testing-post
    "be able to add customers (POST /customers)" in {
      val customer = Customer(UUID.randomUUID(), "Kapi", 42, "jp")
      val customerEntity = Marshal(customer).to[MessageEntity].futureValue // futureValue is from ScalaFutures

      // using the RequestBuilding DSL:
      val request = Post("/customers").withEntity(customerEntity)

      request ~> routes ~> check {
        status should ===(StatusCodes.Created)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        // and we know what message we're expecting back:
        entityAs[String] should ===("""{"description":"customer Kapi created."}""")
      }
    }
    //#testing-post

    "be able to remove customers (DELETE /customers)" in {
      // customer the RequestBuilding DSL provided by ScalatestRouteSpec:
      val request = Delete(uri = "/customers/Kapi")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        // and no entries should be in the list:
        entityAs[String] should ===("""{"description":"customer Kapi deleted."}""")
      }
    }
    //#actual-test
  }
  //#actual-test

  //#set-up
}
//#set-up
//#customer-routes-spec
