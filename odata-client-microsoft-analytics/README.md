# odata-client-microsoft-analytics

Microsoft provides OData endpoints for Analytics for DevOps services. These OData services are extensible in that organizations can extend the standard API with their own data types and functions where allowed by Azure DevOps.

This project generates classes (and includes them in the jar artifact) for accessing these services based on metadata definitions in [src/main/odata](src/main/odata). Currently there are four versions supported 1.0, 2.0, 3.0, 4.0. 

To use the base functionality of an Analytics for DevOps service you need to be part of an organization and have a username and Personal Access Token that will be used in Basic Authentication against the service. Here's an example:

```java
import com.github.davidmoten.microsoft.analytics.Analytics;
import microsoft.vs.analytics.v3.myorg.container.Container;

Container client = Analytics 
  .service(Container.class) 
  .organization(organization) 
  .project(project) // optional
  .version(version) // mandatory
  .basicAuthentication(username, password) 
  .build();

client
  .workItems()
  .metadataMinimal()
  .select("WorkItemId,State,CreatedDate")
  .stream()
  .map(x -> x.getWorkItemId() + ", " + x.getState() + ", " +  x.getCreatedDate()) 
  .forEach(System.out::println); 
```
   
