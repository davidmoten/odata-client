package com.github.davidmoten.odata.client;

public interface CollectionPageEntityRequest<T extends ODataEntity, R extends EntityRequest<T>> {

    CollectionPage<T> get(QueryOption... options);
    
    R id(String id);
    
}
