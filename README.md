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

## MsGraph Client Usage
Here's example usage of the *odata-client-msgraph* artifact (model classes generated from the MsGraph metadata). Let's connect to the Graph API and list all messages in the Inbox that are unread:

```java
GraphService client = MsGraph 
    .tenantName(tenantName) 
    .clientId(clientId) 
    .clientSecret(clientSecret) 
    .refreshBeforeExpiry(5, TimeUnit.MINUTES) 
    .build();
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



