package com.meetpraveen.persistency

import java.util.UUID

import scala.concurrent.{ ExecutionContext, Future, Promise }
import scala.language.implicitConversions

import com.datastax.driver.core.{ Cluster, PreparedStatement, ResultSet, Session, SimpleStatement }
import com.google.common.util.concurrent.{ FutureCallback, Futures, ListenableFuture }
import com.meetpraveen.LogContext
import com.meetpraveen.LogUtils.LogEnhancer
import com.meetpraveen.model.{ Customer, Customers }
import com.meetpraveen.model.Constants.{ cassandraPort, cassandraUrl }

object CqlUtils extends LogContext {

  def execute(statement: Future[PreparedStatement], params: Any*)(implicit executionContext: ExecutionContext, session: Session): Future[ResultSet] = {
    statement.map { x =>
      val para = params.map(identity)
      if (!para.isEmpty) x.bind(para.map(_.asInstanceOf[Object]):_*) else x.bind()
    }
      .flatMap(session.executeAsync(_))
  }

  /* implicit class enables the primary constructor available for implicit conversion
   	in this case, we are creating a string interpolation for prepared statement
  	{{link}} https://www.scala-lang.org/api/2.12.0/scala/StringContext.html
  */
  implicit class CqlEnhancer(val cql: StringContext) extends AnyVal {

    /* def cql(args: Any*)(implicit session: Session): ListenableFuture[PreparedStatement] = {
    	the implicit def for listenableFutureToFuture enables us to replace the line above
    	with the one below
    */
    def cql(args: Any*)(implicit session: Session): Future[PreparedStatement] = {
      val statement = new SimpleStatement(cql.raw(args: _*))
      session.prepareAsync(statement)
    }
  }

  /* Implicit def provides a way to convert the available type to expected type
   * here, available type is ListenableFuture and expected type is Future
   */
  implicit def listenableFutureToFuture[T](listenableFuture: ListenableFuture[T]): Future[T] = {
    val promise = Promise[T]()
    Futures.addCallback(listenableFuture, new FutureCallback[T] {
      def onFailure(error: Throwable): Unit = {
        log"ERROR: Cassandra operation failed - ${error.getMessage}"
        promise.failure(error)
        ()
      }
      def onSuccess(result: T): Unit = {
        log"SUCCESS: Cassandra operation successfull - ${result.toString().take(20)}..."
        promise.success(result)
        ()
      }
    })
    promise.future
  }

  /* Implicit val makes the value of the type available wherever we expect it in
   * the implicit parameter list
   */
  lazy val cluster = new Cluster.Builder().addContactPoints(cassandraUrl).withPort(cassandraPort.toInt).build()
  implicit lazy val session = cluster.connect()
}

// Persistency definition trait
trait Persistency {
  def getCustomers(): Future[Customers]
  def getCustomer(id: UUID): Future[Option[Customer]]
  def upsertCustomer(customer: Customer): Future[Customer]
  def deleteCustomer(id: UUID): Future[Unit]
}

trait CassandraPersistency extends Persistency {
  import com.meetpraveen.persistency.CqlUtils._
  import scala.collection.JavaConverters._
  import scala.concurrent.ExecutionContext.Implicits.global

  override def getCustomers(): Future[Customers] = {
    val query = cql"SELECT * FROM myks.customer"
    val resultSet = execute(query).map(_.asScala.map(row => Customer(UUID.fromString(row.getString("id")), row.getString("name"), row.getInt("age"), row.getString("countryOfResidence"))))
    val resultList = resultSet.map(_.toList)
    resultList.transform(Customers(_), identity)
  }

  override def getCustomer(id: UUID): Future[Option[Customer]] = {
    val query = cql"SELECT * FROM myks.customer WHERE id = ?"
    val resultset = execute(query, id.toString).map(_.asScala.toStream.take(1).map(row => Customer(UUID.fromString(row.getString("id")), row.getString("name"), row.getInt("age"), row.getString("countryOfResidence"))))
    resultset.transform(stream => stream.take(1).toList.headOption, identity)
  }

  override def upsertCustomer(customer: Customer): Future[Customer] = {
    val query = cql"INSERT INTO myks.customer(id, name, age, countryOfResidence) VALUES(?,?,?,?)"
    val rs = execute(query, customer.id.toString, customer.name.toString, customer.age, customer.countryOfResidence.toString)
    rs.transform(_ => customer, identity)
  }

  override def deleteCustomer(id: UUID): Future[Unit] = {
    val query = cql"DELETE from myks.customer WHERE id = ?"
    execute(query, id.toString).transform(_ => (), identity)
  }
}