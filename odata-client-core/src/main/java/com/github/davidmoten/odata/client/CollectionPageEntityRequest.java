package com.github.davidmoten.odata.client;

public interface CollectionPageEntityRequest<T extends ODataEntity, R extends EntityRequest<T>> {

    // TODO hide this method
    CollectionPage<T> get(CollectionEntityRequestOptions options);

    R id(String id);

    default CollectionPage<T> get() {
        return new CollectionEntityRequestOptionsBuilder<T, R>(this).get();
    }

    default CollectionEntityRequestOptionsBuilder<T, R> requestHeader(String key, String value) {
        return new CollectionEntityRequestOptionsBuilder<T, R>(this).requestHeader(key, value);
    }

    default CollectionEntityRequestOptionsBuilder<T, R> search(String clause) {
        return new CollectionEntityRequestOptionsBuilder<T, R>(this).search(clause);
    }

    default CollectionEntityRequestOptionsBuilder<T, R> filter(String clause) {
        return new CollectionEntityRequestOptionsBuilder<T, R>(this).filter(clause);
    }

    default CollectionEntityRequestOptionsBuilder<T, R> orderBy(String clause) {
        return new CollectionEntityRequestOptionsBuilder<T, R>(this).orderBy(clause);
    }

    default CollectionEntityRequestOptionsBuilder<T, R> skip(long n) {
        return new CollectionEntityRequestOptionsBuilder<T, R>(this).skip(n);
    }

    default CollectionEntityRequestOptionsBuilder<T, R> top(long n) {
        return new CollectionEntityRequestOptionsBuilder<T, R>(this).top(n);
    }

}
