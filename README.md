# odata-client
Generates java client for a service described by OData CSDL 4.0 metadata.

Status: *pre-alpha* (in development)

## Features
* optimal type-safety
* builders and method chaining for conciseness and discoverability
* Microsoft Graph v1.0 client
* OData inheritance supported for serialization and derialization

## Constraints
* Just one key (with multiple properties if desired) per entity is supported (secondary keys are ignored in terms of code generation)

## TODO
* use editLink if there in patch (but what about request headers and query options like $filter which may be supported for patch)
* create (POST)
* delete (DELETE)
* support OpenType (arbitrary extra fields get written)
* support EntityContainer inheritance (maybe, no sample that I've found uses it so far)
* support precision, scale (maybe)
* support NavigationPropertyBindings
* more decoupling of model from presentation in generation
* add http client 

