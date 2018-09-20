# odata-client
Generates java client for a service described by OData CSDL 4.0 metadata.

Features
* optimal type-safety
* builders and method chaining for conciseness and discoverability
* Microsoft Graph v1.0 client
* OData inheritance supported for serialization and derialization

Constraints
* Every OData Entity must have a single primary key being an `Edm.String` with name id (can be inherited)


Status: *pre-alpha* (in development)
