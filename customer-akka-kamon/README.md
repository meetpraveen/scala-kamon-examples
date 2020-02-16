#A guide to create a simple scala microservice
The aim of this excercise is to get a headstart on how to create a simple scala microservice. We are going to use the following stack/tools -

1. ***Build/package - sbt
2. Http server/routes - akka http
3. Logging - logback with kamon based correlationId trace
4. Persistency - Cassandra (for the this we will use embedded cassandra)
5. Metrics - Host, JVM, Process, akka, akka-http, futures, executor-service and logback instrumentation using kamon  

#Ports

1. Rest API Service listens on http://localhost:8080
2. Kamon Status Page listens on http://localhost:5266
3. Prometheus end point listens on http://localhost:9095

#REST API
The service exposes the following APIs to support CRUD for customer 

```
GET /customer/v1/api HTTP 1.1 
Sample response - 
< HTTP/1.1 200 OK
< X-Correlation-Id: d20feddb-200e-49d1-a175-5ffcca6e06bc
< Server: akka-http/10.1.9
< Date: Sun, 16 Feb 2020 16:28:34 GMT
< Content-Type: application/json
{
  "customers": [
    {
      "id": "f58fa847-f3e4-47f3-9278-193142a77c0b",
      "name": "praveen",
      "age": 34
    }
  ]
}

POST /customer/v1/api HTTP 1.1 
{
  "name": "praveen",
  "age": 34
} 
Sample response - 201 Created

GET /customer/v1/api/f58fa847-f3e4-47f3-9278-193142a77c0b HTTP 1.1 
Sample response - 200 OK
{ 
  "id": "58fa847-f3e4-47f3-9278-193142a77c0b", 
  "name": "praveen", 
  "age": 34
}

PUT /customer/v1/api/ HTTP 1.1 
{
  "name": "praveen",
  "age": 34
} 
Sample response - 200 OK

DELETE /customer/v1/api/ HTTP 1.1 
Sample response - 200 OK
``` 

#Kamon configuration

1. Correletation id propogation - 
```
kamon {
  #Specify scheme for chosen traceId(correlationId in this case) conversion to response header 
  trace.identifier-scheme = "com.meetpraveen.log.CorrelationIdScheme"
  propagation.http.default {
    tags {
      #Context tag mapping with the response header
      mappings {
        "correlationId" = "X-Correlation-Id"
      }
    }
    #Header reader and handler which is used to set trace context. The handler can be used
    #to validate the incoming header or to seed a new id if its missing from request
    entries.incoming.span = "com.meetpraveen.log.CorrelationIdHeaderReader"
  }
  instrumentation {
    #http-server tracing settings
    http-server.default.tracing {
      #traceId tag to use for trace logs
      preferred-trace-id-tag = "correlationId"
      response-headers {
        #header value to log for traceId 
        trace-id = "X-Correlation-Id"
      }
      #http-server default tracing enabled
      enabled = yes
    }
  }
}
```
2. Akka actor metrics configuration
```
kamon {
  instrumentation {
    akka.filters {
      actors.track {
        #Includes user created actors from actor metrics
        includes = [ "*/user/**" ]
        #Filters out system cretaed actor from metrics
        excludes = [ "*/system/**"]
      }

      dispatchers {
        includes = [ "**" ]
      }

      routers {
        includes = [ "**" ]
      }
    }
  }
}
```
3. Disbaling default modules
If some default modules needs to be disabled, this can be done by these configuration.
In the app these are not done, as we need these important metrics.

```
#kamon.modules.host-metrics.enabled = no
#kamon.modules.jvm-metrics.enabled = no
#kamon.modules.process-metrics.enabled = no
```
