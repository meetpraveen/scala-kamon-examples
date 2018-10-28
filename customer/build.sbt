lazy val akkaHttpVersion = "10.1.4"
lazy val akkaVersion    = "2.5.16"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "com.meetpraveen",
      scalaVersion    := "2.12.6"
    )),
    name := "customer",
    libraryDependencies ++= Seq(
      "com.typesafe.akka"       %% "akka-http"            % akkaHttpVersion,
      "com.typesafe.akka"       %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka"       %% "akka-http-xml"        % akkaHttpVersion,
      "com.typesafe.akka"       %% "akka-stream"          % akkaVersion,

      "com.typesafe.akka"       %% "akka-http-testkit"    % akkaHttpVersion % Test,
      "com.typesafe.akka"       %% "akka-testkit"         % akkaVersion     % Test,
      "com.typesafe.akka"       %% "akka-stream-testkit"  % akkaVersion     % Test,
      "org.scalatest"           %% "scalatest"            % "3.0.5"         % Test,
      //STEP: Cassandra driver for conencting to cassandra db
      "com.datastax.cassandra"  % "cassandra-driver-core"% "3.6.0",
      //STEP: Embedded cassandra, we will be using it to run the test locally and to test it
      "org.cassandraunit"       % "cassandra-unit"       % "3.1.3.2",
      "com.lightbend.akka"      %% "akka-stream-alpakka-cassandra" % "1.0-M1"
    )
  )
