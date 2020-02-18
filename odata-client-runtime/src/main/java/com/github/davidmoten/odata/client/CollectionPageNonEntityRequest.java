package com.github.davidmoten.odata.client;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

@JsonIgnoreType
public class CollectionPageNonEntityRequest<T, R extends NonEntityRequest<T>> {

    private final ContextPath contextPath;
    private final Class<T> cls;
    private final SchemaInfo schemaInfo;

    // should not be public api
    public CollectionPageNonEntityRequest(ContextPath contextPath, Class<T> cls, SchemaInfo schemaInfo) {
        this.contextPath = contextPath;
        this.cls = cls;
        this.schemaInfo = schemaInfo;
    }

    CollectionPage<T> get(CollectionRequestOptions options) {
        ContextPath cp = contextPath.addQueries(options.getQueries());
        HttpResponse r = cp.context().service().get(cp.toUrl(), options.getRequestHeaders());
        return cp.context().serializer().deserializeCollectionPageNonEntity(r.getText(), cls, cp, schemaInfo);
    }

    public CollectionPage<T> get() {
        return new CollectionNonEntityRequestOptionsBuilder<T, R>(this).get();
    }

    public CollectionNonEntityRequestOptionsBuilder<T, R> requestHeader(String key, String value) {
        return new CollectionNonEntityRequestOptionsBuilder<T, R>(this).requestHeader(key, value);
    }

    public CollectionNonEntityRequestOptionsBuilder<T, R> search(String clause) {
        return new CollectionNonEntityRequestOptionsBuilder<T, R>(this).search(clause);
    }

    public CollectionNonEntityRequestOptionsBuilder<T, R> filter(String clause) {
        return new CollectionNonEntityRequestOptionsBuilder<T, R>(this).filter(clause);
    }

    public CollectionNonEntityRequestOptionsBuilder<T, R> orderBy(String clause) {
        return new CollectionNonEntityRequestOptionsBuilder<T, R>(this).orderBy(clause);
    }

    public CollectionNonEntityRequestOptionsBuilder<T, R> skip(long n) {
        return new CollectionNonEntityRequestOptionsBuilder<T, R>(this).skip(n);
    }

    public CollectionNonEntityRequestOptionsBuilder<T, R> top(long n) {
        return new CollectionNonEntityRequestOptionsBuilder<T, R>(this).top(n);
    }

    public CollectionNonEntityRequestOptionsBuilder<T, R> select(String clause) {
        return new CollectionNonEntityRequestOptionsBuilder<T, R>(this).select(clause);
    }

    public CollectionNonEntityRequestOptionsBuilder<T, R> metadataFull() {
        return new CollectionNonEntityRequestOptionsBuilder<T, R>(this).metadataFull();
    }

    public CollectionNonEntityRequestOptionsBuilder<T, R> metadataMinimal() {
        return new CollectionNonEntityRequestOptionsBuilder<T, R>(this).metadataMinimal();
    }

    public CollectionNonEntityRequestOptionsBuilder<T, R> metadataNone() {
        return new CollectionNonEntityRequestOptionsBuilder<T, R>(this).metadataNone();
    }

}
