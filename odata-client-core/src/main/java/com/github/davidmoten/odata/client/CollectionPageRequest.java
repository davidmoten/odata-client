package com.github.davidmoten.odata.client;

public interface CollectionPageRequest<T extends ODataEntity> {

    CollectionPage<T> request(QueryOption... options);

}
