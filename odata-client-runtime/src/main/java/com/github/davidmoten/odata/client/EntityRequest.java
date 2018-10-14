package com.github.davidmoten.odata.client;

public interface EntityRequest<T extends ODataEntity> {

    // TODO customize HTTP headers, add delete, update, patch, put, post, select, search,
    // expand, useCaches
    // TODO make extra methods invisible

    T get(EntityRequestOptions<T> options);

    void delete(EntityRequestOptions<T> options);

    T patch(EntityRequestOptions<T> options, T entity);

    T put(EntityRequestOptions<T> options, T entity);
    
    T post(EntityRequestOptions<T> options, T entity);

    default T get() {
        return new EntityRequestOptionsBuilder<T>(this).get();
    }

    default T patch(T entity) {
        return new EntityRequestOptionsBuilder<T>(this).patch(entity);
    }

    default T put(T entity) {
        return new EntityRequestOptionsBuilder<T>(this).put(entity);
    }
    
    default T post(T entity) {
        return new EntityRequestOptionsBuilder<T>(this).post(entity);
    }

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
