lazy val akkaHttpVersion = "10.1.9"
lazy val akkaVersion    = "2.5.23"

lazy val root = (project in file("."))
  .enablePlugins(JavaAgent)
  .settings(
    inThisBuild(List(
      organization    := "com.meetpraveen",
      scalaVersion    := "2.12.6"
    )),
    name := "customer",
    libraryDependencies ++= Seq(
      "io.kamon" %% "kamon-bundle" % "2.0.5",
      "io.kamon" %% "kamon-prometheus" % "2.0.1",
      "com.typesafe.akka"       %% "akka-http"            % akkaHttpVersion,
      "com.typesafe.akka"       %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka"       %% "akka-http-xml"        % akkaHttpVersion,
      "com.typesafe.akka"       %% "akka-stream"          % akkaVersion,
      "com.typesafe.scala-logging" %% "scala-logging"     % "3.9.2",
      "ch.qos.logback"          % "logback-classic"       % "1.0.9",

      "com.typesafe.akka"       %% "akka-http-testkit"    % akkaHttpVersion % Test,
      "com.typesafe.akka"       %% "akka-testkit"         % akkaVersion     % Test,
      "com.typesafe.akka"       %% "akka-stream-testkit"  % akkaVersion     % Test,
      "org.scalatest"           %% "scalatest"            % "3.0.5"         % Test,
      //STEP: Cassandra driver for conencting to cassandra db
      "com.datastax.cassandra"  % "cassandra-driver-core"% "3.6.0",
      //STEP: Embedded cassandra, we will be using it to run the test locally and to test it
      "org.cassandraunit"       % "cassandra-unit"       % "3.1.3.2",
      "com.lightbend.akka"      %% "akka-stream-alpakka-cassandra" % "1.0-M1"
    ),
    javaAgents += "org.aspectj" % "aspectjweaver" % "1.9.5"
  )
