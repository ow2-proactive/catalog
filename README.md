# ProActive Catalog

[![Build Status](http://jenkins.activeeon.com/buildStatus/icon?job=generic-catalog)](http://jenkins.activeeon.com/job/generic-catalog/)
[![Coverage Status](https://coveralls.io/repos/github/ow2-proactive/catalog/badge.svg?branch=origin%2Fmaster)](https://coveralls.io/github/ow2-proactive/catalog?branch=origin%2Fmaster)

The goal of the catalog is to store ProActive objects. It is the catalog for general purpose.

The stored objects in the catalog could be:
- rule for PCW service
- workflows
- selection script
- Proactive pre/post task
- objects for authentication service
- any kind of other objects

Catalog contains a set of objects organized into buckets with versioning capabilities.

A single Bucket can store multiple kinds of objects

## Building and deploying

You can build a WAR file as follows:

```
$ gradle clean build war
```

Then, you can directly deploy the service with embedded Tomcat:

```
$ java -jar build/libs/catalog-X.Y.Z-SNAPSHOT.war
```

The WAR file produced by Gradle can also be deployed in the embedded Jetty container started by an instance of [ProActive Server](https://github.com/ow2-proactive/scheduling).

## Samples with REST API

Available resources can be listed and tested with Swagger.
A complete documentation of the Catalog REST API is available by default on:

[http://localhost:8080/catalog/swagger-ui.html](http://localhost:8080/catalog/swagger-ui.html)

Below are some REST invocations using [HTTPie](https://github.com/jkbrzt/httpie).

### Bucket resource

Creating a new bucket named _test_:
```
$ http -f POST http://localhost:8080/buckets name=test-bucket
```

Getting information about bucket by name as identifier:
```
$ http http://localhost:8080/buckets/test-bucket
```

Listing available buckets:
```
$ http http://localhost:8080/buckets
```


### Object resource

**REST actions related to existing objects apply to the latest revision of the identified object.**

Adding a new object in bucket with name test-bucket:
```
$ http -f POST http://localhost:8080/buckets/test-bucket/objects file@/path/to/object.xml
```

Getting object metadata for object with name objectName in bucket test-bucket:
```
$ http http://localhost:8080/buckets/test-bucket/objects/objectName
```

Fetching the raw content of a last revision for object with identifier objectName:
```
$ http http://localhost:8080/buckets/{bucketName}/resources/{objectName}/raw
```

Listing objects managed by bucket with name test-bucket:
```
$ http http://localhost:8080/buckets/test-bucket/objects
```

### Object revision resource

Adding a new object revision for object with name objectName in bucket with name test-bucket:
```
$ http -f POST http://localhost:8080/buckets/test-bucket/objects/objectName/revisions file@/path/to/object.xml
```

Getting object metadata for object with identifier objectName in bucket test-bucket for revision identified by commitTime:
```
$ http http://localhost:8080/buckets/test-bucket/objects/objectName/revisions/commitTime
```

Fetching raw object's content for object with identifier objectName and specific revision by commitTime:
```
$ http http://localhost:8080/buckets/{bucketName}/resources/{objectName}/revisions/{commitTime}/raw

```

Listing all revisions for object with name test-bucket:
```
$ http http://localhost:8080/buckets/test-bucket/objects/objectName/revisions
```

## GraphQL usage
GraphQL is the standardized query language, which provides opportunity to search and retrieve the Objects by specified criterion from the Catalog.
NOTE: Please follow the graphiql endpoint: http://localhost:8080/catalog/graphiql in order to query for the specific Object.

In GraphQL the user can filter Objects by specific parameters. The user will retrieve only the fields of Object that are required.

### Example of GraphQL query

The next example will fetch all Objects that has name like `Clear%` from `cloud-automation` bucket.
The response will return all fields, that are in the query.
```
{
  allCatalogObjects(where: {AND: [{nameArg: {like: “Clear%“}}, {bucketNameArg: {eq: "cloud-automation"}}]}) {
    edges {
      bucketName
      name
      kind
      contentType
      metadata {
        key
        value
        label
      }
      commitMessage
      commitDateTime
      link
    }
    page
    size
    totalPage
    totalCount
    hasNext
    hasPrevious
  }
}
```
More complex examples are available inside GraphqlServiceIntegrationTest class implementation.
For more information about existing search filters and model of Catalog Object, please check Documentation explorer in graphiql GUI.
