# hello-lagom-kamon

This project is to show how to integrate kamon 2.0 with a lagom application.

The steps are as follows - 

1. Add required sbt plugins to the project>plugin.sbt
```sbt
// Used for packaging
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.21")
// sbt kanela runner used to inject kamon while running from
// sbt. This is not working currently, but the -javaagent injection
// using JavaAgent plugin below and running from packaged app is working 
addSbtPlugin("io.kamon" % "sbt-kanela-runner" % "2.0.5")
addSbtPlugin("com.lightbend.sbt" % "sbt-javaagent" % "0.1.4")
```
2. Add kamon dependencies to Impl projects
```sbt
val kamonDeps = Seq(
  "io.kamon" %% "kamon-bundle" % "2.0.5",
  "io.kamon" %% "kamon-prometheus" % "2.0.1"
)
(project in file("hello-impl"))
.settings(
    libraryDependencies ++= kamonDeps
)
```
3. Add plugins to Impl projects
```sbt
val kamonDeps = Seq(
  "io.kamon" %% "kamon-bundle" % "2.0.5",
  "io.kamon" %% "kamon-prometheus" % "2.0.1"
)
(project in file("hello-impl"))
.settings(libraryDependencies ++= kamonDeps ++ other ..)
.enablePlugins(
  LagomScala, // Lagom plugin 
  JavaAgent, // JavaAgent plugin
  JavaAppPackaging // Packaging plugin
)
```
4. Add javaagent library and java options
```sbt
lazy val packagingSettings = Seq(
  javaAgents += "io.kamon" % "kanela-agent" % "1.0.5",
  javaOptions in Universal += "-J-XshowSettings:vm ,-Dorg.aspectj.tracing.factory=default"
)
lazy val kamonDeps = Seq(
  "io.kamon" %% "kamon-bundle" % "2.0.5",
  "io.kamon" %% "kamon-prometheus" % "2.0.1"
)
(project in file("hello-impl"))
.settings(libraryDependencies ++= kamonDeps ++ other ..)
.enablePlugins(
  LagomScala, // Lagom plugin 
  JavaAgent, // JavaAgent plugin
  JavaAppPackaging // Packaging plugin
)
.settings(packagingSettings)
```
5. Add docker settings
```sbt
lazy val dockerSettings = Seq(
  dockerUpdateLatest := false,
  dockerBaseImage := "openjdk:8-jdk-slim", // JDK required for kamon instrumentation
  version := "1.0-SNAPSHOT",
  dockerExposedPorts := Seq(9000, 9095, 5266)
)
lazy val packagingSettings = Seq(
  javaAgents += "io.kamon" % "kanela-agent" % "1.0.5",
  javaOptions in Universal += "-J-XshowSettings:vm ,-Dorg.aspectj.tracing.factory=default"
)
lazy val kamonDeps = Seq(
  "io.kamon" %% "kamon-bundle" % "2.0.5",
  "io.kamon" %% "kamon-prometheus" % "2.0.1"
)
(project in file("hello-impl"))
.settings(libraryDependencies ++= kamonDeps ++ other ..)
.enablePlugins(
  LagomScala, // Lagom plugin 
  JavaAgent, // JavaAgent plugin
  JavaAppPackaging // Packaging plugin
)
.settings(packagingSettings)
.settings(dockerSettings)
```
6. Deploy app in minikube
```shell script
# minikube start
minikube start --memory 6000 #Version enforced for reactive-sandbox
minikube addons enable ingress
eval $(minikube docker-env)
kubectl apply -f deploy/rbac.yml

#kafka, zookeeper and cassandra deployment
docker-compose -f deploy/data-services/docker-compose.yml up -d

#build and deploy application
sbt docker:publishLocal

host=`ifconfig en0 | grep broadcast | cut -d' ' -f2`
export CASSANDRA_URL=$host:9042
export KAFKA_BROKER_URL=$host:9094

cat deploy/hello.yml | \
sed "s/{{CASSANDRA_URL}}/$CASSANDRA_URL/g" | \
sed "s/{{KAFKA_BROKER_URL}}/$KAFKA_BROKER_URL/g" | \
kubectl apply -f -

cat deploy/hello-stream.yml | \
sed "s/{{CASSANDRA_URL}}/$CASSANDRA_URL/g" | \
sed "s/{{KAFKA_BROKER_URL}}/$KAFKA_BROKER_URL/g" | \
kubectl apply -f -

kubectl apply -f deploy/ingress.yml
kubectl get pods

NAME                                          READY     STATUS             RESTARTS   AGE
hello-stream-67775f58db-2h7jv   1/1       Running            0          27m
hello-86cdd499c4-2jklj          1/1       Running            0          27m
```
7. Testing routes
```shell script
minikube ip
192.168.64.10

# Add this entry to /etc/hosts
192.168.64.10 minikube.local

curl minikube.local/api/hello/praveen
Hello, praveen!%

curl minikube.local/prom/hello
```

Now you can access app at http://minikube.local/api/hello/praveen
Kamon prometheus endpoint - http://minikube.local/prom/hello

8. Cleaning it up

```shell script
kubectl delete deployment hello
kubectl delete deployment hello-stream
kubectl delete service hello
kubectl detele service hello-stream
kubectl delete ingress hello
docker-compose if deploy/data-services/docker-compose.yml down
(minikube delete || true) &>/dev/null
```
