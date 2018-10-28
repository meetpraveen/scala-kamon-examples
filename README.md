# A guide to create a simple scala microservice
The aim of this excercise is to get a headstart on how to create a simple scala microservice. We are going to use the following stack/tools -

1. Build/package - sbt
2. Ide - eclipse (scala IDE)
3. Http server/routes - akka http
4. Logging - logback
5. Persistency - Cassandra (for the excercise we will use embedded cassandra)

With these we will be creating a simple customer service which support CRUD for a customer resource

```
GET /customer/v1/api HTTP 1.1 
Sample response - 200 OK 
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

# Create project

1. We will use sbt as our build and package tooling. There are a number of templates one can start from, some of these are maintained as giter8 templates and are good starting point.

`sbt -Dsbt.version=0.13.15 new akka/akka-http-quickstart-scala.g8`

	_We can start without template as well, in that case one needs configure and specify the project config details on their own._

	`sbt new`
2. Now that we have the project please jump on to the code example in the customer folder. It has the necessary documentation to complete the implementation. For the impatient refer to the branch 'implementation' for the concrete implementation.

