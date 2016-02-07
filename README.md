# ProActive Workflow Catalog

[![Build Status](http://jenkins.activeeon.com/job/workflow-catalog/badge/icon)](http://jenkins.activeeon.com/job/workflow-catalog/)
[![Coverage Status](https://coveralls.io/repos/github/ow2-proactive/workflow-catalog/badge.svg?branch=origin%2Fmaster)](https://coveralls.io/github/ow2-proactive/workflow-catalog?branch=origin%2Fmaster)

The purpose of the workflow catalog is to store ProActive workflows.

A workflow catalog is subdivided into buckets. 

Each bucket manages zero, one or more versioned ProActive workflows.

## Building and deploying

You can build a WAR file as follows:

```
$ gradle clean build war
```

Then, you can directly deploy the service with embedded Tomcat:

```
$ java -jar build/libs/workflow-catalog-X.Y.Z-SNAPSHOT.war
```

The WAR file produced by Gradle can also be deployed in the embedded Jetty container started by an instance of [ProActive Server](https://github.com/ow2-proactive/scheduling).

## Samples

Available resources can be listed and tested with Swagger:

[http://localhost:8080/swagger](http://localhost:8080/swagger)

Below are some REST invocations using [HTTPie](https://github.com/jkbrzt/httpie).

### Bucket resource

Creating a new bucket named _test_:
```
$ http -f POST http://localhost:8080/buckets name=test
```

Getting information about bucket with identifier 1:
```
$ http http://localhost:8080/buckets/1
```

Listing available buckets:
```
$ http http://localhost:8080/buckets
```

Listing available buckets targeting page 42:

```
$ http http://localhost:8080/buckets?page=42
```

### Workflow resource

**REST actions related to existing workflows apply to the latest revision of the identified workflow.**

Adding a new workflow in bucket with identifier 1:
```
$ http -f POST http://localhost:8080/buckets/1/workflows file@/path/to/workflow.xml
```

Getting workflow metadata for workflow with identifier 1 in bucket with id 1:
```
$ http http://localhost:8080/buckets/1/workflows/1
```

Fetching workflow XML payload for workflow with identifier 1 in bucket with id 1:
```
$ http http://localhost:8080/buckets/1/workflows/1?alt=xml
```

Listing workflows managed by bucket with identifier 1:
```
$ http http://localhost:8080/buckets/1/workflows
```

### Workflow revision resource

Adding a new workflow revision for workflow with identifier 1 in bucket with id 1:
```
$ http -f POST http://localhost:8080/buckets/1/workflows/1/revisions file@/path/to/workflow.xml
```

Getting workflow metadata for workflow with identifier 1 and revision 1 in bucket with id 1:
```
$ http http://localhost:8080/buckets/1/workflows/1/revisions/1
```

Fetching workflow XML payload for workflow with identifier 1 and revision 1 in bucket with id 1:
```
$ http http://localhost:8080/buckets/1/workflows/1/revisions/1?alt=xml
```

Listing all revisions for workflow with identifier 1:
```
$ http http://localhost:8080/buckets/1/workflows/1/revisions
```
