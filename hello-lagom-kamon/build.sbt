import com.lightbend.lagom.core.LagomVersion

organization in ThisBuild := "com.meetpraveen"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.6"

lazy val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.3" % "provided"
lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.8" % Test
lazy val kamonDeps = Seq(
  "io.kamon" %% "kamon-bundle" % "2.0.5",
  "io.kamon" %% "kamon-prometheus" % "2.0.1"
)
lazy val akkaDiscoveryDeps = Seq(
  "com.lightbend.akka.discovery" %% "akka-discovery-kubernetes-api" % "1.0.5",
  "com.lightbend.lagom" %% "lagom-scaladsl-akka-discovery-service-locator" % LagomVersion.current
)

lazy val `hello` = (project in file("."))
  .aggregate(`hello-api`, `hello-impl`, `hello-stream-api`, `hello-stream-impl`)

lazy val `hello-api` = (project in file("hello-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `hello-impl` = (project in file("hello-impl"))
  .enablePlugins(LagomScala, JavaAppPackaging, JavaAgent)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslCluster,
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      macwire,
      scalaTest
    ) ++ akkaDiscoveryDeps ++ kamonDeps
  )
  .settings(packagingSettings)
  .settings(lagomForkedTestSettings)
  .settings(dockerSettings)
  .dependsOn(`hello-api`)

lazy val `hello-stream-api` = (project in file("hello-stream-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `hello-stream-impl` = (project in file("hello-stream-impl"))
  .enablePlugins(LagomScala, JavaAppPackaging, JavaAgent)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslCluster,
      lagomScaladslTestKit,
      macwire,
      scalaTest
    ) ++ akkaDiscoveryDeps ++ kamonDeps
  )
  .settings(dockerSettings)
  .settings(packagingSettings)
  .dependsOn(`hello-stream-api`, `hello-api`)

lazy val dockerSettings = Seq(
  dockerUpdateLatest := false,
  dockerBaseImage := "openjdk:8-jdk-slim"
)

lazy val packagingSettings = Seq(
  javaAgents += "io.kamon" % "kanela-agent" % "1.0.5",
  javaOptions in Universal ++= Seq(
    "-Dpidfile.path=/dev/null",
    "-J-XshowSettings:vm",
    "-Dorg.aspectj.tracing.factory=default"
  )
)

lagomKafkaEnabled in ThisBuild := false
lagomKafkaAddress in ThisBuild := "localhost:9092"

lagomCassandraEnabled in ThisBuild := false
lagomUnmanagedServices in ThisBuild := Map("cas_native" -> "http://localhost:9042")

