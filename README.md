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

## Constraints
* Just one key (with multiple properties if desired) per entity is supported (secondary keys are ignored in terms of code generation)

## TODO
* support OpenType (arbitrary extra fields get written)
* support EntityContainer inheritance (maybe, no sample that I've found uses it so far)
* support precision, scale (maybe)
* support NavigationPropertyBindings
* more decoupling of model from presentation in generation

## MsGraph SDK Usage
Here's example usage of the *odata-client-msgraph* artifact (model classes generated from the MsGraph metadata). Let's connect to the Graph API and list all messages in the Inbox that are unread:

```java
GraphService client = MsGraph //
    .tenantName(tenantName) //
    .clientId(clientId) //
    .clientSecret(clientSecret) //
    .refreshBeforeExpiry(5, TimeUnit.MINUTES) //
    .build();
String mailbox = "me";
client.users(mailbox) //
    .mailFolders("Inbox") //
    .messages() //
    .filter("isRead eq false") //
    .expand("attachments") //
    .get() //
    .stream() //
    .map(x -> x.getSubject().orElse("")) //
    .forEach(System.out::println);
```



