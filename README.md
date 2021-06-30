# odata-client
<a href="https://github.com/davidmoten/odata-client/actions/workflows/ci.yml"><img src="https://github.com/davidmoten/odata-client/actions/workflows/ci.yml/badge.svg"/></a><br/>
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.davidmoten/odata-client-runtime/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/com.github.davidmoten/odata-client)<br/>
[![codecov](https://codecov.io/gh/davidmoten/odata-client/branch/master/graph/badge.svg)](https://codecov.io/gh/davidmoten/odata-client)<br/>

Generates java client for a service described by OData v4 metadata. Includes client libraries for Microsoft Graph primary and beta APIs, Graph Explorer API, Azure Analytics for DevOps APIs, and Microsoft Dynamics CRM.

Status : *released to Maven Central* 

## Features
* High level of type safety in generated code
* Many unit tests (real service calls are made and recorded then used for unit tests)
* Builders and method chaining for conciseness and discoverability
* Immutability! (generated objects from the model are immutable)
* OData inheritance supported for serialization and derialization
* Interschema references supported (Thompson-Reuters Datascope API uses interschema references)
* Http calls using java.net.URLConnection or using Apache HttpClient
* Collections are `Iterable` and streamable (via `.stream()`)
* Paging is handled for you automatically when iterating collections
* Bound and unbound actions and functions are supported
* Custom requests supported via `client._custom()`
* Generated code is very clean - well formatted, no redundant imports ([example](src/docs/FileAttachment.java))
* Microsoft Graph v1.0 client
* Microsoft Graph Beta client
* Graph Explorer client (test endpoint)
* Microsoft Analytics for DevOps 1.0, 2.0, 3.0, 4.0 and custom [clients](odata-client-microsoft-analytics/README.md).
* Microsoft Dynamics CRM client
* More generated clients can be added, just raise an issue
* Runs on Java 8+ (including Java 11+). When running <11 the jaxb dependencies can be excluded from odata-client-runtime. 

## How to build
`mvn clean install`

## Background
OData is an OASIS standard for building and consuming REST APIs. The *odata-client* project focuses only on OData HTTP APIs implemented using JSON payloads. A client of an OData service can be generated completely from the metadata document published by the service. An example is the Microsoft Graph Odata [metadata](odata-client-generator/src/main/odata/msgraph-metadata.xml).

The main actively supported java clients for OData 4 services are [Apache Olingo](https://github.com/apache/olingo-odata4) and the [SDL OData Framework](https://github.com/sdl/odata). However, neither of these projects generate all of the code you might need. Olingo generates some code but you still have to read the metadata xml to know what you can do with the generated classes. This project *odata-client* generates nearly all the code you need so that you just follow auto-complete on the available methods to navigate the service.

Microsoft Graph is an OData 4 service with a Java SDK being developed at https://github.com/microsoftgraph/msgraph-sdk-java. Progress is slow and steady (but happening) on that client (8 Nov 2018) and it can do a lot already. My [frustrations](https://github.com/davidmoten/odata-client/wiki/Microsoft-msgraph-sdk-java-problems-addressed-by-odata-client) with the design of that client gave rise to an investigation into generating clients for OData services in general and that investigation turned into this project.

## How to generate java classes for an OData service
Add *odata-client-maven-plugin* and *build-helper-maven-plugin* to your `pom.xml` as per [odata-client-msgraph/pom.xml](odata-client-msgraph/pom.xml). You'll also need to save a copy of the service metadata (at http://SERVICE_ROOT/$metadata) to a file in your `src/odata` directory. Once everything is in place a build of your maven project will generate the classes and make them available as source (that's what the *build-helper-maven-plugin* does).

Your plugin section will look like this:
```xml
<plugin>
   <groupId>com.github.davidmoten</groupId>
   <artifactId>odata-client-maven-plugin</artifactId>
   <version>VERSION_HERE</version>
   <executions>
     <execution>
       <id>generate-odata-models</id>
       <phase>generate-sources</phase>
       <goals>
         <goal>generate</goal>
       </goals>
       <configuration>
         <metadata>src/main/odata/PATH_TO_THE_METADATA_XML_FILE</metadata>
         <schemas>
           <schema>
            <namespace>NAMESPACE_IN_YOUR_METADATA_FILE</namespace>
            <packageName>package.of.generated.classes</packageName>
           </schema>
           <!-- More schemas -->
         </schemas>
       </configuration>
     </execution>
   </executions>
</plugin>
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>build-helper-maven-plugin</artifactId>
    <version>3.2.0</version>
     <executions>
      <execution>
        <id>add-source</id>
         <phase>generate-sources</phase>
         <goals>
           <goal>add-source</goal>
         </goals>
        <configuration>
          <sources>
            <source>${project.build.directory}/generated-sources/java</source>
          </sources>
        </configuration>
     </execution>
    </executions>
</plugin>
```
The sources will be generated to your `target/generated-sources/java` folder and you need to copy them to your project.

### How to create a Client for your OData Service (non-Microsoft API) with Bearer Tokens. 

This example considers a custom OData api available at `$API_ENDPOINT` that authorizes using Bearer tokens in the Authorization header. The custom api has a users list that we want to return with a call to the service. The example has a hardcoded token for simplicity.

The `SchemaInfo` List should contain the `SchemaInfos` generated for your service metadata. (See generated classes in packages ending in `.schema`).

```java
    HttpClientBuilder b =
        HttpClientBuilder
            .create()
            .useSystemProperties();
            
    BearerAuthenticator authenticator =
        new BearerAuthenticator(
            () ->
                "INSERT_TOKEN_HERE",
            "$API_END_POINT");
            
    HttpService httpService =
        new ApacheHttpClientHttpService( //
            new Path(
                "$API_END_POINT",
                PathStyle.IDENTIFIERS_AS_SEGMENTS),
            b::build,
            authenticator::authenticate);
            
    final List<com.github.davidmoten.odata.client.SchemaInfo> schemaInfos =
        Arrays.asList(generate.pkg.schema.SchemaInfo.INSTANCE);
        
    Context context =
        new Context(Serializer.INSTANCE, httpService, Collections.emptyMap(), schemaInfos);

    // in this example Container is the name of the Container element in the metadata
    Container client = new Container(context);
    List<String> userEmails =
        client.users()
	.stream()
        .map(User::getEmail)
        .filter(Optional::isPresent)
        .map(x -> x.get())
        .collect(Collectors.toList());
```
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

You can also use artifactId *odata-client-msgraph-beta* to access the beta endpoint (and they have different package names so you can use both clients in the same project if you want).

If you are running on less than Java 11 then you can exclude some dependencies but only if you are running *odata-client* version < 0.1.46 (from 0.1.46 jaxb libraries moved to the jakarta jaxb libraries):
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

### Create a client for the Graph Explorer
There's a web test page for queries called the [Graph Explorer](https://developer.microsoft.com/en-us/graph/graph-explorer). You can use the client against this service for experiments. Bear in mind that the Graph Explorer service doesn't support lots but it is still a good place to get a feel for it.

```java
GraphService client = MsGraph.explorer().build();
```

or behind a proxy:

```java
GraphService client = MsGraph
  .explorer()
  .proxyHost(proxyHost)
  .proxyPort(8080)
  .build();
```
Here's an example:

```java
client 
  .users()
  .select("displayName")
  .stream() 
  .limit(10)
  .map(user -> user.getDisplayName().orElse("?"))
  .forEach(System.out::println);
```

output:

```
Conf Room Adams
Adele Vance
MOD Administrator
Alex Wilber
Allan Deyoung
Conf Room Baker
Ben Walters
Brian Johnson (TAILSPIN)
Christie Cline
Conf Room Crystal
```

Here's a contrived example with Graph Explorer that gets 5 attachment names from messages and their sizes. The example makes good use of the streaming capabilities of the odata-client API:

```java
GraphService client = MsGraph.explorer().build();
client
  .me()
  .messages()
  .select("id")
  .stream()
  .flatMap(m -> m.getAttachments().select("name, size").stream())
  .limit(5)
  .map(a -> a.getName().orElse("?") + " " + a.getSize().orElse(-1) + "B")
  .forEach(System.out::println);
```

output:
```
analytics_icon.png 2281B
lock_circle_teal.png 2713B
collab_hero_left_2x.png 6496B
ProfileImage_320_48d31887-5fad-4d73-a9f5-3c356e68a038.png 335675B
collab_hero_right_2x.png 6611B
```


### Create a client for Graph v1.0
The first step is to create a client that will be used for all calls in your application.

```java
GraphService client = MsGraph 
    .tenantName(tenantName) 
    .clientId(clientId) 
    .clientSecret(clientSecret) 
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .refreshBeforeExpiry(5, TimeUnit.MINUTES) 
    .build();
```
### Create a client for Graph v1.0 behind a proxy

```java
GraphService client = MsGraph 
    .tenantName(tenantName) 
    .clientId(clientId) 
    .clientSecret(clientSecret) 
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .refreshBeforeExpiry(5, TimeUnit.MINUTES) 
    .proxyHost(proxyHost)
    .proxyPort(proxyPort)
    .proxyUsername(proxyUsername)
    .proxyPassword(proxyPassword)
    .build();
```

### Specify the authentication endpoint
This client supports Client Credential authentication only at the moment. Raise an issue if you need a different sort.

To change the authentication endpoint used to retrieve access tokens (the default is AuthenticationEndpoint.GLOBAL):

```java
GraphService client = MsGraph
    .tenantName(tenantName)
    .clientId(clientId)
    .clientSecret(clientSecret)
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .refreshBeforeExpiry(5, TimeUnit.MINUTES)
    .authenticationEndpoint(AuthenticationEndpoint.GERMANY)
    .build();
``` 

If you want to do really complicated things with proxies or http in general you can use the `.httpClientProvider` or `.httpClientBuilderExtras` methods (coupled to Apache *HttpClient*). 

### Usage example 1 - simple
Here's example usage of the *odata-client-msgraph* artifact (model classes generated from the MsGraph metadata). Let's connect to the Graph API and list all messages in the Inbox that are unread. Note that paging is completely handled for you in the `.stream()` method!

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
### Collections and paging
*odata-client* handles the annoying paging stuff under the covers so you don't have to worry about it. Let's look at some examples.

We'll get the list of users:

```java
List<User> users = client.users().toList();
```
That was easy wasn't it! Under the covers *odata-client* kept calling pages (using `@odata.nextLink`) and building the list to return. This won't always be a good idea because the Collection might be very big and you might not want all the data anyway. What are some alternatives?

`client.users()` is itself an `Iterable` so you can do this:

```java
for (User user: client.users()) 
  System.out.println(user);
```
or 
```java
Iterator<User> it = client.users().iterator();
while (it.hasNext()) 
  System.out.println(it.next());
```

You can use `.stream()`:

```java
client.users()
  .stream() 
  .forEach(System.out::println);
```
Again, all paging is taking care of. When the current page runs out the library requests another one.

If you do want to do explicit paging then you can do this:

```java
CollectionPage<User> users = client.users().get();
```
`CollectionPage` has methods `currentPage` and `nextPage`.

#### Your own page size
You can request a different page size than the default via a special HTTP request header but the server *may choose to ignore your request*:

```java
List<User> users = client.users().maxPageSize(200).get().currentPage();
```
So what if you want to receive the collection in custom size pages regardless of the page size being delivered by the server? *odata-client* has two utility methods to help you out. Let's chop the stream of User into pages of 15 elements:

```java
import com.github.davidmoten.odata.client.Util;

Iterator<List<User>> it = Util.buffer(client.users().iterator(), 15);
```
or with streams:
```java
Stream<List<User>> users = Util.buffer(client.users().stream(), 15);
```

#### Getting a collection from a link
Some users need stateless paging so want to recommence the next page from the `odata.nextLink` value in the page json. Here's an example: 

```java
String nextLink = "https://...";
List<User> users = client
  .users()
  .urlOverride(nextLink)
  .get()
  .currentPage();
```

#### Getting json for the collection
Some users want to do a passthrough of Graph API responses in JSON format through to their clients. You can obtain a simplified collection page response as JSON like this:

```java
CollectionPage<User> page = client.users().get();
String json = page.toJsonMinimal();
```

#### Filtering a collection by type
To only return items of a certain type from a collection request (this is called "restriction to instances of the derived type" in the OData specification):

```java

client
  .directoryObjects()
  .filter(User.class)
  .forEach(System.out::println);
```

### Updating Microsoft Graph metadata
Developer instructions:
```bash
cd odata-client-msgraph
./update-metadata.sh
cd ..
## check if still builds!
mvn clean install
```
Note that the latest metadata is downloaded from microsoft but that also:
* `HasStream="true"` is added to EntityType `itemAttachment`
* `HasStream="true"` is added to EntityType `message`

URLs are:
* https://graph.microsoft.com/v1.0/$metadata 
* https://graph.microsoft.com/beta/$metadata

### Timeouts
Connect and read timeouts can be specified when creating the client and apply globally:

```java
GraphService client = MsGraph
    .tenantName(tenantName)
    .clientId(clientId)
    .clientSecret(clientSecret)
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .refreshBeforeExpiry(5, TimeUnit.MINUTES)
    .authenticationEndpoint(AuthenticationEndpoint.GERMANY)
    .build();
``` 

Global timeouts can be overriden **by request**:

```java
List<User> users = client
  .users()
  .connectTimeout(10, TimeUnit.SECONDS)
  .readTimeout(30, Timeout.SECONDS)
  .toList();
```
### Download an email in SMTP MIME format
Use `Message.getStream()`.

Here's an example using Graph Explorer where we download the MIME format of a random email returned by the service (you can run this exact code yourself to test):
```java
GraphService client = MsGraph.explorer().build();
String mimeMessage = 
  client
    .me()
    .messages()
    .stream()
    .findFirst()
    .get()
    .getStream()
    .get()
    .getStringUtf8();
System.out.println(mimeMessage);
```
Output:

```
From: Sundar Ganesan <admin@m365x1515.onmicrosoft.com>
To: Megan Bowen <MeganB@M365x214355.onmicrosoft.com>, Alex Wilber
Subject:
Thread-Index: AQHWhnph+xDTvdQDAJzfEoyf/g4tCA==
X-MS-Exchange-MessageSentRepresentingType: 1
Date: Wed, 9 Sep 2020 07:25:22 +0000
Message-ID: 1599636322761
Content-Language: en-US
X-MS-Exchange-Organization-SupervisoryReview-TeamsContext:
	{"SenderId":"19806f8c015344149305dc48012b4789","AadGroupId":null,"ThreadId":"19:650081f4700a4414ac15cd7993129f80@thread.v2","MessageId":1599636322761,"ParentMessageId":null,"TenantId":"91c1bcb4-2349-4abd-83c1-6ae4ffaf7f6c","ThreadType":"chat","CorrelationVector":"KuDTOICw9UWVpDHto11XFw.1.1.1.2441281688.1.0"}
X-MS-Has-Attach:
X-MS-TNEF-Correlator:
X-MS-Exchange-Organization-RecordReviewCfmType: 0
Content-Type: multipart/alternative; boundary="_000_1599636322761_"
MIME-Version: 1.0

--_000_1599636322761_
Content-Type: text/plain; charset="us-ascii"

test

--_000_1599636322761_
Content-Type: text/html; charset="us-ascii"

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=us-ascii">
</head>
<body>
test
</body>
</html>

--_000_1599636322761_--
```

### Sending an email with an attachment

Unfortunately sending an email with an attachment is complicated somewhat by Microsoft Graph forcing users to use two different methods to upload an attachment depending on the size of the attachment. Default maximum size for an attachment is apparently 25MB according to Microsoft [docs](https://docs.microsoft.com/en-us/office365/servicedescriptions/exchange-online-service-description/exchange-online-limits#message-limits) though an administrator can increase the limit up to 150MB if required.

To help send an email a helper utility class called `Email` exists. Here's an example that sends an email with two attachments:

```java
Email
  .mailbox(mailbox) 
  .subject("hi there " + new Date())
  .body("hello there how are you")
  .to("davidmoten@gmail.com")
  .attachment(file)
  .name("list.txt")
  .contentMimeType("text/plain")
  .chunkSize(512 * 1024)
  .attachment("hi there")
  .name("greeting.txt")
  .contentMimeType("text/plain")
  .send(client);
```

The builder code above does quite a lot for you. For reference here's how you do it using the generated classes only:

```java
GraphService client = ...;
File file = ...;
String contentType = "text/plain";
String mailbox = "me@somewhere.com";

// create new outbound mail in Drafts folder
MailFolderRequest drafts = client //
    .users(mailbox) //
    .mailFolders("Drafts");
Message m = Message.builderMessage() //
    .subject("hi there " + new Date()) //
    .body(ItemBody.builder() //
            .content("hello there how are you") //
            .contentType(BodyType.TEXT).build()) //
    .from(Recipient.builder() //
            .emailAddress(EmailAddress.builder() //
                    .address(mailbox) //
                    .build()) //
            .build()) //
    .toRecipients(Recipient.builder() //
            .emailAddress(EmailAddress.builder() //
                    .address("someone@thing.com") //
                    .build()) //
            .build()) //
    .build();

m = drafts.messages().post(m);

// Upload attachment to the new mail
// We use different methods depending on the size of the attachment
// because will fail if doesn't match the right size window
if (file.length() < 3000000) {
    client.users(mailbox) //
        .messages(m.getId().get()) //
        .attachments() //
        .post(FileAttachment.builderFileAttachment() //
           .name(file.getName()) //
           .contentBytes(Files.readAllBytes(file.toPath())) //
           .contentType(contentType).build());
} else {
    AttachmentItem a = AttachmentItem //
        .builder() //
        .attachmentType(AttachmentType.FILE) //
        .contentType(contentType) //
        .name(file.getName()) //
        .size(file.length()) //
        .build();
    
    int chunkSize = 500*1024;
    client //
        .users(mailbox) //
        .messages(m.getId().get()) //
        .attachments() //
        .createUploadSession(a) //
        .get() //
        .putChunked() //
        .readTimeout(10, TimeUnit.MINUTES) //
        .upload(file, chunkSize, Retries.builder().maxRetries(2).build());
}

// send the email 
// you can't just use m.send().call() because Graph doesn't 
// honour the path corresponding to m for send method and throws
// a 405 status code
client
  .users(mailbox)
  .messages(m.getId().get())
  .send()
  .call();
```
### Delta collections
Some functions return delta collections which track changes to resource collections. In the Graph API v1.0 there are delta functions on users, calendar events, and messages. 

Here's an example of usage:

```java
// get a delta that returns no users but marks where future deltas will be based on
CollectionPage<User> delta = client.users().delta().deltaTokenLatest().get();

// a while later
delta = delta.nextDelta().get();

// delta is a CollectionPage and can be iterated upon or paged through as you like
// if you don't iterate through the collection the next call to 
// nextDelta() will do it for you because the deltaLink is on the last page 
// of the current collection 

// print out all users that have changed
delta.forEach(System.out::println);

// a while later
delta = delta.nextDelta().get();
...

```
A special streaming method is available also that returns a list of wrapped delta objects then the delta link to use for the next delta call:

```java
Stream<ObjectOrDeltaLink<User>> delta = 
    client
      .users()
      .delta()
      .get()
      .streamWithDeltaLink();
```
Using the delta link from the last element of the stream the next delta call can be made like so:

```java
Stream<ObjectOrDeltaLink<User>> delta = 
    client 
      .users()
      .overrideUrl(deltaLink)
      .get()
      .streamWithDeltaLink();
```

`ObjectOrDeltaLink` is serializable to JSON via its Jackson annotations and at least one user is using the `streamWithDeltaLink` method to pass large deltas over a network via WebFlux (see issue [#44](https://github.com/davidmoten/odata-client/issues/44)).

## Expand
Special support is provided for the `$expand` option. Here's an example:

```java
Attachment attachment = client
  .users(emailId)
  .mailFolders(folderId)
  .messages(messageId)
  .attachments(attachmentId)
  .expand("microsoft.graph.itemattachment/item")
  .get();
if (attachment instanceof ItemAttachment) {
    ItemAttachment a = (ItemAttachment) attachment;
    OutlookItem item = a.getItem().get();
    ...
}
```

When the above code is called, the line `OutlookItem item = a.getItem().get()` will not actually make a network call but rather use the json in unmapped fields of attachment. If the json is not present in the unmapped fields then a network call will be made.

This support is present for both Entity requests and Enitty Collection requests.

## Logging
The default http client Apache *httpclient* uses *Apache Commons Logging* and the odata-client libraries use *slf4j*. To get full access to all logs you'll need to ensure that the right adapters are present that pipe logs to your preferred logging library. Tests in *odata-client-msgraph* demonstrate the use of *log4j* as the preferred logger. You'll note that these dependencies are present:

```xml
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>jcl-over-slf4j</artifactId>
    <version>${slf4j.version}</version>
</dependency>

 <dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-slf4j-impl</artifactId>
    <version>${log4j.version}</version>
</dependency>
```
A configuration file for log4j is also present (in `src/test/resources` but you might want it in `src/main/resources`):

**log4j2.xml**
```
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="WARN">
  <Appenders>
    <Console name="Console" target="SYSTEM_OUT">
      <PatternLayout pattern="%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/>
    </Console>
  </Appenders>
  <Loggers>
    <Root level="INFO">
      <AppenderRef ref="Console"/>
    </Root>
  </Loggers>
</Configuration>
```
If you want DEBUG logging just set the Root level to DEBUG.

## Serialization
*odata-client* generated classes are annotated with Jackson JSON annotations specifically to support internal serialization and deserialization for communication with the service. Since 0.1.20 entities are annotated with `@JsonInclude(Include.NON_NULL)` so that default serialization with Jackson will exclude null values (internally this annotation is overriden for certain use cases such as when we want tell the service to update a field to null).

## Custom requests
To get more precise control over the interaction with an OData api you can use the methods of `CustomRequest`. Here's an example that posts json to the service and recieves a json response:

```java
CustomRequest c = client._custom().withRelativeUrls();
String json = c.getString("me/mailFolders/inbox/messages?$select=id&count=true", RequestOptions.EMPTY, RequestHeader.ODATA_VERSION);
System.out.println(json);
```
Output:
```
{
	"@odata.context": "https://graph.microsoft.com/v1.0/$metadata#users('48d31887-5fad-4d73-a9f5-3c356e68a038')/mailFolders('inbox')/messages(id)",
	"@odata.count": 21,
	"@odata.nextLink": "https://graph.microsoft.com/v1.0/me/mailFolders/inbox/messages?$select=id&count=true&$skip=10",
	"value": [
		{
			"@odata.etag": "W/\"CQAAABYAAAAiIsqMbYjsT5e/T7KzowPTAAL0pwit\"",
			"id": "AAMkAGVmMDEzMTM4LTZmYWUtNDdkNC1hMDZiLTU1OGY5OTZhYmY4OABGAAAAAAAiQ8W967B7TKBjgx9rVEURBwAiIsqMbYjsT5e-T7KzowPTAAAAAAEMAAAiIsqMbYjsT5e-T7KzowPTAAL0sBcuAAA="
		},
                ...
	]
}

```
## Usage Notes
### Streams
To find the read url for a property that is of type `Edm.Stream` you generally need to read the entity containing the stream property with the `Accept: odata.metadata=full` request header (set `.metadataFull()` before calling `get()` on an entity). As of 0.1.54 this request header is the default for Media Entities or entities with stream properties (but not for collections of these entities).

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
* more decoupling of model from presentation in generation
* use annotations (docs) in javadoc
* support function composition
* support `count`
* support `raw`
* support $ref
* support updating/creating streams
* support geographical primitive types (where's the spec?!!)
* support references to other metadata files (imports)
* auto-rerequest with odata.metadata=full header if Edm.Stream is read
* support TypeDefinition
* only generate classes that are actually used (e.g. not every Entity or ComplexType needs a corresponding Collection request)
* remove context property "modify.stream.edit.link" from MsGraph client once they support their own @odata.mediaEditLink!
* <strike>add helper methods to generated UploadUrl class in MsGraph</strike>
* <strike>add optional retry logic to StreamUploaderChunked</strike>
* increase chunked upload efficiency by writing read bytes to destination immediately, retaining the ability to retry the chunk
