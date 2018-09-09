package com.github.davidmoten.odata.client;

public interface EntityRequest<T extends ODataEntity> {

    T get(QueryOption... options);
    
}
