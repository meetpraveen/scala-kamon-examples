package com.meetpraveen.persistency

import com.datastax.driver.core.Session
import com.meetpraveen.persistency.CqlUtils.session
import org.cassandraunit.CQLDataLoader
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet
import org.cassandraunit.utils.EmbeddedCassandraServerHelper

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