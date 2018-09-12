package com.github.davidmoten.odata.client;

public interface EntityRequest<T extends ODataEntity> {

    // TODO customize HTTP headers, add delete, update, patch, select, search,
    // expand, useCaches
    T get(SingleEntityRequestOptions<T> options);

}
