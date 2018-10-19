package com.github.davidmoten.odata.client;

public abstract class EntityRequest<T extends ODataEntity> {

    // TODO customize HTTP headers, add delete, update, patch, put, post, select,
    // search,
    // expand, useCaches
    // TODO make extra methods invisible

    public abstract T get(EntityRequestOptions<T> options);

    public abstract void delete(EntityRequestOptions<T> options);

    public abstract T patch(EntityRequestOptions<T> options, T entity);

    public abstract T put(EntityRequestOptions<T> options, T entity);

    public T get() {
        return new EntityRequestOptionsBuilder<T>(this).get();
    }

    public void delete() {
        new EntityRequestOptionsBuilder<T>(this).delete();
    }

    public T patch(T entity) {
        return new EntityRequestOptionsBuilder<T>(this).patch(entity);
    }

    public T put(T entity) {
        return new EntityRequestOptionsBuilder<T>(this).put(entity);
    }

    public EntityRequestOptionsBuilder<T> requestHeader(String key, String value) {
        return new EntityRequestOptionsBuilder<T>(this).requestHeader(key, value);
    }

    public EntityRequestOptionsBuilder<T> select(String clause) {
        return new EntityRequestOptionsBuilder<T>(this).select(clause);
    }

    public EntityRequestOptionsBuilder<T> expand(String clause) {
        return new EntityRequestOptionsBuilder<T>(this).expand(clause);
    }

}
