package com.meetpraveen.hello.impl

import akka.cluster.sharding.typed.scaladsl.Entity
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.server._
import com.meetpraveen.hello.api.HelloService
import com.softwaremill.macwire._
import javax.inject.{Provider, Singleton}
import kamon.Kamon
import play.api.libs.ws.ahc.AhcWSComponents

class HelloLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication = {
    new HelloApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }
  }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication = {
    val ki = wire[KamonInitializer]
    new HelloApplication(context) with LagomDevModeComponents
  }

  override def describeService = Some(readDescriptor[HelloService])
}

abstract class HelloApplication(context: LagomApplicationContext)
  extends MyLagomApplication(context)
    with CassandraPersistenceComponents
    with LagomKafkaComponents
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer: LagomServer = serverFor[HelloService](wire[HelloServiceImpl])

  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry: JsonSerializerRegistry = HelloSerializerRegistry

  // Initialize the sharding of the Aggregate. The following starts the aggregate Behavior under
  // a given sharding entity typeKey.
  clusterSharding.init(
    Entity(HelloState.typeKey)(
      entityContext => HelloBehavior.create(entityContext)
    )
  )

}

abstract class MyLagomApplication(context: LagomApplicationContext) extends LagomApplication(context) with KamonModule

case class KamonInit()
object KamonInit {
  def apply(s: String): KamonInit = {
    Kamon.init()
    new KamonInit()
  }
}

@Singleton
class KamonInitializer {
  val kamonInit = KamonInit("test")
}

trait KamonModule {
  val kamonInit = wire[KamonInitializer]
}