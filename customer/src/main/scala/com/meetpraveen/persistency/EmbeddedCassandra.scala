package com.meetpraveen.persistency

import org.cassandraunit.CQLDataLoader
import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import com.datastax.driver.core.Session
import org.joda.time.DateTime
import com.datastax.driver.core.Cluster
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet
import scala.util.Try
import scala.util.Success
import com.meetpraveen.model.Constants._
import org.apache.cassandra.config.Config
import com.meetpraveen.persistency.CqlUtils.session

object EmbeddedCassandra {

  def init() = {
    EmbeddedCassandraServerHelper.startEmbeddedCassandra(EmbeddedCassandraServerHelper.DEFAULT_CASSANDRA_YML_FILE, 3 * 60000L)
    loadSchema(session)
  }

  private def loadSchema(session: Session) = {
    val dataLoader = new CQLDataLoader(session);
    dataLoader.load(new ClassPathCQLDataSet("customer.cql", "myks"));
  }
}