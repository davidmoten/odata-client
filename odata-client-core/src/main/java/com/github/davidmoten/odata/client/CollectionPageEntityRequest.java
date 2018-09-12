package com.github.davidmoten.odata.client;

public interface CollectionPageEntityRequest<T extends ODataEntity, R extends EntityRequest<T>> {

    CollectionPage<T> get(CollectionRequestOptions options);

    R id(String id);

    default CollectionPage<T> get() {
        return new CollectionRequestOptionsBuilder<T, R>(this).get();
    }
    
    default CollectionRequestOptionsBuilder<T, R> requestHeader(String key, String value) {
        return new CollectionRequestOptionsBuilder<T, R>(this).requestHeader(key, value);
    }

    default CollectionRequestOptionsBuilder<T, R> search(String clause) {
        return new CollectionRequestOptionsBuilder<T, R>(this).search(clause);
    }

    default CollectionRequestOptionsBuilder<T, R> filter(String clause) {
        return new CollectionRequestOptionsBuilder<T, R>(this).filter(clause);
    }

    default CollectionRequestOptionsBuilder<T, R> orderBy(String clause) {
        return new CollectionRequestOptionsBuilder<T, R>(this).orderBy(clause);
    }

    default CollectionRequestOptionsBuilder<T, R> skip(long n) {
        return new CollectionRequestOptionsBuilder<T, R>(this).skip(n);
    }

    default CollectionRequestOptionsBuilder<T, R> top(long n) {
        return new CollectionRequestOptionsBuilder<T, R>(this).top(n);
    }

}
