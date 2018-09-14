package com.github.davidmoten.odata.client;

public interface EntityRequest<T extends ODataEntity> {

    // TODO customize HTTP headers, add delete, update, patch, select, search,
    // expand, useCaches
    // TODO make extra methods invisible

    T get(SingleEntityRequestOptions<T> options);

    T delete(SingleEntityRequestOptions<T> options);

    T update(SingleEntityRequestOptions<T> options);

    T patch(SingleEntityRequestOptions<T> options);

    default SingleEntityRequestOptionsBuilder<T> requestHeader(String key, String value) {
        return new SingleEntityRequestOptionsBuilder<T>(this).requestHeader(key, value);
    }

    default SingleEntityRequestOptionsBuilder<T> select(String clause) {
        return new SingleEntityRequestOptionsBuilder<T>(this).select(clause);
    }

    default SingleEntityRequestOptionsBuilder<T> expand(String clause) {
        return new SingleEntityRequestOptionsBuilder<T>(this).expand(clause);
    }

}
