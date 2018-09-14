package com.github.davidmoten.odata.client;

public interface EntityRequest<T extends ODataEntity> {

    // TODO customize HTTP headers, add delete, update, patch, select, search,
    // expand, useCaches
    // TODO make extra methods invisible

    T get(EntityRequestOptions<T> options);

    T delete(EntityRequestOptions<T> options);

    T update(EntityRequestOptions<T> options);

    T patch(EntityRequestOptions<T> options);

    default EntityRequestOptionsBuilder<T> requestHeader(String key, String value) {
        return new EntityRequestOptionsBuilder<T>(this).requestHeader(key, value);
    }

    default EntityRequestOptionsBuilder<T> select(String clause) {
        return new EntityRequestOptionsBuilder<T>(this).select(clause);
    }

    default EntityRequestOptionsBuilder<T> expand(String clause) {
        return new EntityRequestOptionsBuilder<T>(this).expand(clause);
    }

}
