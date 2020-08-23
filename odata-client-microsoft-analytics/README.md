# odata-client-microsoft-analytics

Microsoft provides OData endpoints for Analytics for DevOps services. These OData services are extensible in that organizations can extend the standard API with their own data types and functions where allowed by Azure DevOps.

This project generates classes (and includes them in the jar artifact) for accessing these services based on metadata definitions in [src/main/odata](src/main/odata). Currently there are four versions supported 1.0, 2.0, 3.0, 4.0 but any additional version or specific project metadata can be included in the code generation by copying this project and modifying the pom.xml to generate from a new metadata file. 

## Example
To use the base functionality of an Analytics for DevOps service you need to be part of an organization and have a username and Personal Access Token that will be used in Basic Authentication against the service. The Personal Access Token will need the Analytics Service permission to utilize the OData endpoint. Here's an example:

```java
import com.github.davidmoten.microsoft.analytics.Analytics;
import microsoft.vs.analytics.v3.myorg.container.Container;

Container client = Analytics 
  .service(Container.class) 
  .organization(organization) 
  .project(project) // optional
  .version("v3.0") // mandatory
  .basicAuthentication(username, personalAccessToken) 
  .build();

client
  .workItems()
  .metadataMinimal()
  .select("WorkItemId,State,CreatedDate")
  .stream()
  .map(x -> x.getWorkItemId() + ", " + x.getState() + ", " +  x.getCreatedDate()) 
  .forEach(System.out::println); 
```

## Custom clients
If your OData service has some customizations that are reflected in the metadata (either at the organization level or the organization+project level) then you can generate classes to handle your custom types and functions like this:

* copy this project
* in `src/main/odata` place your custom metadata file (from https://analytics.dev.azure.com/{organization}/_odata/v3.0/$metadata for instance)
* add an execution to the odata-client-maven-plugin section in pom.xml 
* modify the execution to reference the new metadata file and reflect the package mappings you desire for the generated code
* `mvn clean install`

At this point the built jar contains what you need to build a client and make calls against the service.

Then follow the same client creation code as per the example above but import the `Container` class from your generated code instead.

## TODO
* add support for [OData extensions](https://docs.oasis-open.org/odata/odata-data-aggregation-ext/v4.0/cs01/odata-data-aggregation-ext-v4.0-cs01.html)
