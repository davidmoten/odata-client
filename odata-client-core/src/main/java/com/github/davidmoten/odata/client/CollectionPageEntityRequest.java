package com.github.davidmoten.odata.client;

public final class CollectionPageEntityRequest<T extends ODataEntity, R extends EntityRequest<T>> {

    private final ContextPath contextPath;
    private final EntityRequestFactory<T, R> entityRequestFactory;

    // should not be public api
    public CollectionPageEntityRequest(ContextPath contextPath, EntityRequestFactory<T, R> entityRequestFactory) {
        this.contextPath = contextPath;
        this.entityRequestFactory = entityRequestFactory;
    }

    // not public api
    CollectionPage<T> get(CollectionEntityRequestOptions options) {
        return null;
    }

    public R id(String id) {
        return entityRequestFactory.create(contextPath, id);
    }

    public CollectionPage<T> get() {
        return new CollectionEntityRequestOptionsBuilder<T, R>(this).get();
    }

    public CollectionEntityRequestOptionsBuilder<T, R> requestHeader(String key, String value) {
        return new CollectionEntityRequestOptionsBuilder<T, R>(this).requestHeader(key, value);
    }

    public CollectionEntityRequestOptionsBuilder<T, R> search(String clause) {
        return new CollectionEntityRequestOptionsBuilder<T, R>(this).search(clause);
    }

    public CollectionEntityRequestOptionsBuilder<T, R> filter(String clause) {
        return new CollectionEntityRequestOptionsBuilder<T, R>(this).filter(clause);
    }

    public CollectionEntityRequestOptionsBuilder<T, R> orderBy(String clause) {
        return new CollectionEntityRequestOptionsBuilder<T, R>(this).orderBy(clause);
    }

    public CollectionEntityRequestOptionsBuilder<T, R> skip(long n) {
        return new CollectionEntityRequestOptionsBuilder<T, R>(this).skip(n);
    }

    public CollectionEntityRequestOptionsBuilder<T, R> top(long n) {
        return new CollectionEntityRequestOptionsBuilder<T, R>(this).top(n);
    }

}
