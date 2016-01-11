# Workflow Catalog

[![Build Status](http://jenkins.activeeon.com/job/workflow-catalog/badge/icon)](http://jenkins.activeeon.com/job/workflow-catalog/)

The purpose of the workflow catalog is to store ProActive workflows.

A workflow catalog is subdivided into buckets. 

Each bucket has a unique name and store zero, one or more versioned ProActive workflows.

## Building and deploying

You can build a WAR file as follows:

```
gradle clean build war
```

Then, you can directly deploy the service with embedded Jetty:

```
java -jar build/libs/workflow-catalog-7.2.0-SNAPSHOT.war
```

## Samples

Available resources can be listed and tested with Swagger:

[http://localhost:8080/swagger](http://localhost:8080/swagger)

Below are some REST invocations using [HTTPie](https://github.com/jkbrzt/httpie):

TODO
