# odata-client
Generates java client for a service described by OData CSDL 4.0 metadata. 

Status: *pre-alpha* (in development)

## Features
* high level of type safety
* builders and method chaining for conciseness and discoverability
* Immutability! (generated objects from the model are immutable)
* OData inheritance supported for serialization and derialization
* Interschema references supported (Thompson-Reuters uses interschema references)
* Http calls using java.net.URLConnection or using Apache HttpClient
* Collections are `Iterable` and streamable (via `.stream()`)
* Microsoft Graph v1.0 client

## How to build
`mvn clean install`

## How to generate java classes for an OData service
Add *odata-client-maven-plugin* and *build-helper-maven-plugin* to your `pom.xml` as per [odata-client-msgraph/pom.xml](odata-client-plugin/pom.xml). You'll also need to save a copy of the service metadata (at http://SERVICE_ROOT/$metadata) to a file in your `src/odata` directory. Once everything is in place a build of your maven project will generate the classes and make them available as source (that's what the *build-helper-maven-plugin* does).

## Limitations
* Just one key (with multiple properties if desired) per entity is supported (secondary keys are ignored in terms of code generation). I've yet to come across a service that uses multiple keys but it is possible.

## MsGraph Client 
Use this dependency:

```xml
<dependency>
    <groupId>com.github.davidmoten</groupId>
    <artifactId>odata-client-msgraph</artifactId>
    <version>VERSION_HERE</version>
</dependency>
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
client.users(mailbox) 
    .mailFolders("Inbox") 
    .messages() 
    .filter("isRead eq false") 
    .expand("attachments") 
    .get() 
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
    .get() //
    .stream() //
    .count(); //

// Prepare a new message
String id = UUID.randomUUID().toString().substring(0, 6);
Message m = Message
    .builderMessage() //
    .subject("hi there " + id) //
    .body(ItemBody.builder() //
            .content("hello there how are you") //
            .contentType(BodyType.TEXT).build()) //
    .from(Recipient.builder() //
            .emailAddress(EmailAddress.builder() //
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
    .get() //
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
## TODO
* support OpenType (arbitrary extra fields get written)
* support EntityContainer inheritance (maybe, no sample that I've found uses it so far)
* support precision, scale (maybe)
* support NavigationPropertyBindings
* more decoupling of model from presentation in generation
* use annotations (docs) in javadoc
* support functions
* support actions
* support `count`
* support `raw`
* support geographical primitive types (where's the spec?!!)
* support references to other metadata files (imports)
* support Edm.Stream type
* auto-rerequest with odata.metadata=full header if Edm.Stream is read

## Usage Notes
### Streams
To find the read url for a property that is of type `Edm.Stream` you generally need to read the entity containing the stream property with the `Accept: odata.metadata=full` request header (set `.metadataFull()` before calling `get()` on an entity).

## Implementation Notes
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




