# ProActive Catalog

[![Build Status](http://jenkins.activeeon.com/buildStatus/icon?job=catalog)](http://jenkins.activeeon.com/job/catalog/)
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

### Object resource

**REST actions related to existing objects apply to the latest revision of the identified object.**

Adding a new object in bucket with identifier 1:
```
$ http -f POST http://localhost:8080/buckets/1/objects file@/path/to/object.xml
```

Getting object metadata for object with identifier 1 in bucket with id 1:
```
$ http http://localhost:8080/buckets/1/objects/1
```

Fetching object XML payload for object with identifier 1 in bucket with id 1:
```
$ http http://localhost:8080/buckets/1/objects/1?alt=xml
```

Listing objects managed by bucket with identifier 1:
```
$ http http://localhost:8080/buckets/1/objects
```

### object revision resource

Adding a new object revision for object with identifier 1 in bucket with id 1:
```
$ http -f POST http://localhost:8080/buckets/1/objects/1/revisions file@/path/to/object.xml
```

Getting object metadata for object with identifier 1 and revision 1 in bucket with id 1:
```
$ http http://localhost:8080/buckets/1/objects/1/revisions/1
```

Fetching object XML payload for object with identifier 1 and revision 1 in bucket with id 1:
```
$ http http://localhost:8080/buckets/1/objects/1/revisions/1?alt=xml
```

Listing all revisions for object with identifier 1:
```
$ http http://localhost:8080/buckets/1/objects/1/revisions
```
