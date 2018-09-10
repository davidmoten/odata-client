package com.github.davidmoten.odata.client;

public interface CollectionPageRequest<T extends ODataEntity, R extends EntityRequest<T>> {

    CollectionPage<T> get(QueryOption... options);
    
    R id(String id);
    
}
