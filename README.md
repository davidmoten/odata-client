# odata-client
<a href="https://travis-ci.org/davidmoten/odata-client"><img src="https://travis-ci.org/davidmoten/odata-client.svg"/></a><br/>
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.davidmoten/odata-client/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/com.github.davidmoten/odata-client)<br/>
[![codecov](https://codecov.io/gh/davidmoten/odata-client/branch/master/graph/badge.svg)](https://codecov.io/gh/davidmoten/odata-client)<br/>

Generates java client for a service described by OData v4 metadata. Includes client libraries for Microsoft Graph primary and beta APIs.

Status: *in development* 

## Features
* High level of type safety in generated code
* Many unit tests (real service calls are made and recorded then used for unit tests)
* Builders and method chaining for conciseness and discoverability
* Immutability! (generated objects from the model are immutable)
* OData inheritance supported for serialization and derialization
* Interschema references supported (Thompson-Reuters Datascope API uses interschema references)
* Http calls using java.net.URLConnection or using Apache HttpClient
* Collections are `Iterable` and streamable (via `.stream()`)
* Paging is handled for you automatically when iterating large collections
* Bound actions and functions are supported (unbound in the [TODO] list)
* Generated code is very clean - well formatted, no redundant imports
* Microsoft Graph v1.0 client
* Microsoft Graph Beta client
* More generated clients can be added, just raise an issue
* Runs on Java 8+ (including Java 11+). When running <11 the jaxb dependencies can be excluded from odata-client-runtime. 

## How to build
`mvn clean install`

## Background
OData is an OASIS standard for building and consuming REST APIs. The *odata-client* project focuses only on OData HTTP APIs implemented using JSON payloads. A client of an OData service can be generated completely from the metadata document published by the service. An example is the Microsoft Graph Odata [metadata](odata-client-generator/src/main/odata/msgraph-metadata.xml).

The main actively supported java clients for OData 4 services are [Apache Olingo](https://github.com/apache/olingo-odata4) and the [SDL OData Framework](https://github.com/sdl/odata). However, neither of these projects generate all of the code you might need. Olingo generates some code but you still have to read the metadata xml to know what you can do with the generated classes. This project *odata-client* generates nearly all the code you need so that you just follow auto-complete on the available methods to navigate the service.

Microsoft Graph is an OData 4 service with a Java SDK being developed at https://github.com/microsoftgraph/msgraph-sdk-java. Progress is slow and steady (but happening) on this client (8 Nov 2018) and it can do a lot already. My frustrations with the design of that client gave rise to an investigation into generating clients for OData services in general and that investigation turned into this project.

## How to generate java classes for an OData service
Add *odata-client-maven-plugin* and *build-helper-maven-plugin* to your `pom.xml` as per [odata-client-msgraph/pom.xml](odata-client-plugin/pom.xml). You'll also need to save a copy of the service metadata (at http://SERVICE_ROOT/$metadata) to a file in your `src/odata` directory. Once everything is in place a build of your maven project will generate the classes and make them available as source (that's what the *build-helper-maven-plugin* does).

## Limitations
* Just one key (with multiple properties if desired) per entity is supported (secondary keys are ignored in terms of code generation). I've yet to come across a service that uses multiple keys but it is possible.
* see [TODO](#todo)

## MsGraph Client 
Use this dependency:

```xml
<dependency>
    <groupId>com.github.davidmoten</groupId>
    <artifactId>odata-client-msgraph</artifactId>
    <version>VERSION_HERE</version>
</dependency>
```

If you are running on less than Java 11 then you can exclude some dependencies:
```xml
<dependency>
    <groupId>com.github.davidmoten</groupId>
    <artifactId>odata-client-msgraph</artifactId>
    <version>VERSION_HERE</version>
    <exclusions>
        <exclusion>
            <groupId>javax.activation</groupId>
            <artifactId>javax.activation-api</artifactId>
        </exclusion>
        <exclusion>
            <groupId>com.sun.xml.bind</groupId>
            <artifactId>jaxb-core</artifactId>
        </exclusion>
        <exclusion>
            <groupId>com.sun.xml.bind</groupId>
            <artifactId>jaxb-impl</artifactId>
        </exclusion>
        <exclusion>
            <groupId>javax.xml.bind</groupId>
            <artifactId>jaxb-api</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

As slf4j is used for logging you may wish to exclude the *slf4j-api* dependency and add the slf4j logging adapter dependency (e.g. *slf4j-log4j12*) for your logging library:

```xml
	<exclusion>
	    <groupId>org.slf4j</groupId>
	    <artifactId>slf4j-api</artifactId>
	</exclusion>
```

### Create a client
The first step is to create a client that will be used for all calls in your application.

```java
GraphService client = MsGraph 
    .tenantName(tenantName) 
    .clientId(clientId) 
    .clientSecret(clientSecret) 
    .refreshBeforeExpiry(5, TimeUnit.MINUTES) 
    .build();
```
### Usage example 1 - simple
Here's example usage of the *odata-client-msgraph* artifact (model classes generated from the MsGraph metadata). Let's connect to the Graph API and list all messages in the Inbox that are unread:

```java
String mailbox = "me";
client
    .users(mailbox) 
    .mailFolders("Inbox") 
    .messages() 
    .filter("isRead eq false") 
    .expand("attachments") 
    .stream() 
    .map(x -> x.getSubject().orElse("")) 
    .forEach(System.out::println);
```
### Usage example 2 - more complex

Here's a more complex example which does the following:

* Counts the number of messages in the *Drafts* folder
* Prepares a new message
* Saves the message in the *Drafts* folder
* Changes the subject of the saved message
* Checks the subject has changed by reloading
* Checks the count has increased by one
* Deletes the message

```java
MailFolderRequest drafts = client //
    .users(mailbox) //
    .mailFolders("Drafts");

// count number of messages in Drafts
long count = drafts.messages() //
    .metadataNone() //
    .stream() //
    .count(); //

// Prepare a new message
String id = UUID.randomUUID().toString().substring(0, 6);
Message m = Message
    .builderMessage() //
    .subject("hi there " + id) //
    .body(ItemBody
        .builder() //
        .content("hello there how are you") //
        .contentType(BodyType.TEXT) 
        .build()) //
    .from(Recipient
         .builder() //
         .emailAddress(EmailAddress
             .builder() //
             .address(mailbox) //
             .build())
         .build())
    .build();

// Create the draft message
Message saved = drafts.messages().post(m);

// change subject
client //
    .users(mailbox) //
    .messages(saved.getId().get()) //
    .patch(saved.withSubject("new subject " + id));

// check the subject has changed by reloading the message
String amendedSubject = drafts //
    .messages(saved.getId().get()) //
    .get() //
    .getSubject() //
    .get();
if (!("new subject " + id).equals(amendedSubject)) {
    throw new RuntimeException("subject not amended");
}

long count2 = drafts //
    .messages() //
    .metadataNone() //
    .stream() //
    .count(); //
if (count2 != count + 1) {
    throw new RuntimeException("unexpected count");
}

// Delete the draft message
drafts //
    .messages(saved.getId().get()) //
    .delete();
```
### Updating Microsoft Graph metadata
Developer instructions:
* Copy latest from https://graph.microsoft.com/v1.0/$metadata and place in `odata-client-generator/src/main/odata/msgraph-metadata.xml` then format it using `xmllint --format <input> ><output>`
* Update the Entity ItemAttachment to have HasStream="true" attribute (still missing from msgraph metadata)
* Copy latest from https://graph.microsoft.com/beta/$metadata and place in `odata-client-msgraph-beta/src/main/odata/msgraph-beta-metadata.xml` then format it using `xmllint --format <input> ><output>`

## Usage Notes
### Streams
To find the read url for a property that is of type `Edm.Stream` you generally need to read the entity containing the stream property with the `Accept: odata.metadata=full` request header (set `.metadataFull()` before calling `get()` on an entity).

## Implementation Notes
### Generator
Some of the metadata access patterns are O(N<sup>2</sup>) in the generator but does not appear to be significant. If you think it is then the library can start using maps for lookups.

### HasStream
Suppose Person has a Navigation Property of Photo then using the TripPin service example, calling HTTP GET of 

https://services.odata.org/V4/(S(itwk4e1fqfe4tchtlieb5rhb))/TripPinServiceRW/People('russellwhyte')/Photo

returns:
```json
{
"@odata.context": "http://services.odata.org/V4/(S(itwk4e1fqfe4tchtlieb5rhb))/TripPinServiceRW/$metadata#Photos/$entity",
"@odata.id": "http://services.odata.org/V4/(S(itwk4e1fqfe4tchtlieb5rhb))/TripPinServiceRW/Photos(2)",
"@odata.editLink": "http://services.odata.org/V4/(S(itwk4e1fqfe4tchtlieb5rhb))/TripPinServiceRW/Photos(2)",
"@odata.mediaContentType": "image/jpeg",
"@odata.mediaEtag": "W/\"08D6394B7BA10B11\"",
"Id": 2,
"Name": "My Photo 2"
}
```
We then grab the `@odata.editLink` url and call that with `/$value` on the end to GET the photo bytes:

https://services.odata.org/V4/(S(itwk4e1fqfe4tchtlieb5rhb))/TripPinServiceRW/Photos(2)/$value

### Generated classes, final fields and setting via constructors
The initial design for setting fields was via constructors with final fields but Msgraph Beta service goes for gold on the number of fields and well exceeds the JVM limit of 256 with the `Windows10GeneralConfiguration` entity. As a consequence we have private theoretically mutable fields and a protected no-arg constructor. In practice though the fields are not mutable as the only creation access is through a `builder()` or `with*(...)` which return new instances. 

### PATCH support
If choosing the JVM Http client (via `HttpURLConnection`) then the HTTP verb `PATCH` is not supported. However, if using this client and a `ProtocolException` is thrown then the client attempts the call using the special request header `X-HTTP-Override-Method` and the verb `POST`. The detection of the `ProtocolException` is cached and future `PATCH` calls will then use the alternative methods (for the runtime life of the application).

## TODO
* support OpenType (arbitrary extra fields get written)
* support EntityContainer inheritance (maybe, no sample that I've found uses it so far)
* support precision, scale (maybe)
* support NavigationPropertyBindings (just a documentation nicety? Looks like it's just to indicate contains relationships)
* more decoupling of model from presentation in generation
* use annotations (docs) in javadoc
* support unbound actions
* support unbound functions
* support function composition
* support `count`
* support `raw`
* support geographical primitive types (where's the spec?!!)
* support references to other metadata files (imports)
* auto-rerequest with odata.metadata=full header if Edm.Stream is read
* support TypeDefinition
* only generate classes that are actually used (e.g. not every Entity or ComplexType needs a corresponding Collection request)
* implement `CollectionPageNonEntity`
* remove context property "modify.stream.edit.link" from MsGraph client once they support their own @odata.mediaEditLink!
* allow user to specify by-request read and data timeouts 
