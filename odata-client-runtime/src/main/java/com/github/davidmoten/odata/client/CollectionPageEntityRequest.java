package com.github.davidmoten.odata.client;

import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.github.davidmoten.odata.client.internal.RequestHelper;

@JsonIgnoreType
public class CollectionPageEntityRequest<T extends ODataEntityType, R extends EntityRequest<T>> {

    private final ContextPath contextPath;
    private final Class<T> cls;
    private final EntityRequestFactory<T, R> entityRequestFactory;
    private final SchemaInfo schemaInfo;

    // should not be public api
    public CollectionPageEntityRequest(ContextPath contextPath, Class<T> cls,
            EntityRequestFactory<T, R> entityRequestFactory, SchemaInfo schemaInfo) {
        this.contextPath = contextPath;
        this.entityRequestFactory = entityRequestFactory;
        this.cls = cls;
        this.schemaInfo = schemaInfo;
    }

    CollectionPage<T> get(CollectionRequestOptions options) {
        ContextPath cp = options.getEntityType().map(t -> contextPath.addSegment(t)).orElse(contextPath).addQueries(options.getQueries());
        List<RequestHeader> h = RequestHelper.cleanAndSupplementRequestHeaders(options, "minimal",
                false);
        HttpResponse r = cp.context().service().get(cp.toUrl(), h);
        return cp.context().serializer().deserializeCollectionPageNonEntity(r.getText(), cls, cp,
                schemaInfo, h);
    }

    T post(CollectionRequestOptions options, T entity) {
        return RequestHelper.post(entity, contextPath, cls, options, schemaInfo);
    }

    public R id(String id) {
        return entityRequestFactory.create(contextPath.addKeys(new NameValue(id)));
    }

    public CollectionPage<T> get() {
        return new CollectionEntityRequestOptionsBuilder<T, R>(this).get();
    }

    public Stream<T> stream() {
        return get().stream();
    }

    public T post(T entity) {
        return new CollectionEntityRequestOptionsBuilder<T, R>(this).post(entity);
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

    public CollectionEntityRequestOptionsBuilder<T, R> select(String clause) {
        return new CollectionEntityRequestOptionsBuilder<T, R>(this).select(clause);
    }

    public CollectionEntityRequestOptionsBuilder<T, R> entityType(String entityType) {
        return new CollectionEntityRequestOptionsBuilder<T, R>(this).entityType(entityType);
    }

    public CollectionEntityRequestOptionsBuilder<T, R> metadataFull() {
        return new CollectionEntityRequestOptionsBuilder<T, R>(this).metadataFull();
    }

    public CollectionEntityRequestOptionsBuilder<T, R> metadataMinimal() {
        return new CollectionEntityRequestOptionsBuilder<T, R>(this).metadataMinimal();
    }

    public CollectionEntityRequestOptionsBuilder<T, R> metadataNone() {
        return new CollectionEntityRequestOptionsBuilder<T, R>(this).metadataNone();
    }

}
