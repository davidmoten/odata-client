package com.github.davidmoten.odata.client;

import static com.github.davidmoten.odata.client.internal.Util.odataTypeName;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.github.davidmoten.odata.client.internal.RequestHelper;

@JsonIgnoreType
public class CollectionPageEntityRequest<T extends ODataEntityType, R extends EntityRequest<T>> implements Iterable<T> {

    private final ContextPath contextPath;
    private final Class<T> cls;
    private final EntityRequestFactory<T, R> entityRequestFactory;
    // the value for the collection if it has already been obtained (e.g. via expand option)
    private final Optional<Object> value;

    // should not be public api
    public CollectionPageEntityRequest(ContextPath contextPath, Class<T> cls,
            EntityRequestFactory<T, R> entityRequestFactory, Optional<Object> value) {
        this.contextPath = contextPath;
        this.entityRequestFactory = entityRequestFactory;
        this.cls = cls;
        this.value = value;
    }

    CollectionPage<T> get(CollectionRequestOptions options) {
        if (value.isPresent()) {
            Map<String, Object> map = new HashMap<>();
            map.put("value", value.get());
            String json = Serializer.INSTANCE.serialize(map);
            return Serializer.INSTANCE.deserializeCollectionPage( //
                    json, //
                    cls, //
                    contextPath, //
                    Collections.emptyList(), //
                    HttpRequestOptions.EMPTY, //
                    null);
        } else {
            // perform service request
            ContextPath cp = contextPath.addQueries(options.getQueries());
            List<RequestHeader> h = RequestHelper.cleanAndSupplementRequestHeaders(options, "minimal", false);
            HttpResponse r = cp.context().service().get(options.getUrlOverride().orElse(cp.toUrl()), h, options);
            RequestHelper.checkResponseCodeOk(cp, r);
            return cp.context().serializer().deserializeCollectionPage(r.getText(), cls, cp, h, options,
                    null);
        }
    }

    T post(CollectionRequestOptions options, T entity) {
        return RequestHelper.post(entity, contextPath, cls, options);
    }

    public R id(Object id) {
        return entityRequestFactory.create(contextPath.addKeys(new NameValue(id, Object.class)));
    }

    public CollectionPage<T> get() {
        return new CollectionEntityRequestOptionsBuilder<T, R>(this).get();
    }
    
    @Override
    public Iterator<T> iterator() {
        return get().iterator();
    }
    
    public Stream<T> stream() {
        return get().stream();
    }
    
    public Stream<ObjectOrDeltaLink<T>> streamWithDeltaLink() {
        return get().streamWithDeltaLink();
    }
    
    public <S> S to(Function<? super CollectionPage<T>,? extends S> function) {
    	return function.apply(get());
    }
    
    public List<T> toList() {
        return get().toList();
    }

    public T post(T entity) {
        return new CollectionEntityRequestOptionsBuilder<T, R>(this).post(entity);
    }
    
    /**
     * Returns a request for only those members of the collection that are of the
     * requested type. This is referred to in the <a href=
     * "http://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part2-url-conventions.html">OData
     * 4.01 specification</a> as a "restriction to instances of the derived type".
     * 
     * @param <S>
     *            the type ("derived type") to be restricting to
     * @param cls
     *            the Class of the type to restrict to
     * @return a request for a collection of instances with the given type
     */
    @SuppressWarnings("unchecked")
    public <S extends T> CollectionPageEntityRequest<S, EntityRequest<S>> filter(Class<S> cls) {
        return new CollectionPageEntityRequest<S, EntityRequest<S>>(contextPath.addSegment(odataTypeName(cls)), cls,
                (EntityRequestFactory<S, EntityRequest<S>>) entityRequestFactory, value);
    }
    
    public CollectionEntityRequestOptionsBuilder<T, R> requestHeader(String key, String value) {
        return new CollectionEntityRequestOptionsBuilder<T, R>(this).requestHeader(key, value);
    }

    public CollectionEntityRequestOptionsBuilder<T, R> query(String name, String value) {
        return new CollectionEntityRequestOptionsBuilder<T, R>(this).query(name, value);
    }
    
    public CollectionEntityRequestOptionsBuilder<T, R> requestHeader(RequestHeader header) {
        return new CollectionEntityRequestOptionsBuilder<T, R>(this).requestHeader(header);
    }

    public CollectionEntityRequestOptionsBuilder<T, R> maxPageSize(int size) {
        return new CollectionEntityRequestOptionsBuilder<T, R>(this).maxPageSize(size);
    }

    public CollectionEntityRequestOptionsBuilder<T, R> search(String clause) {
        return new CollectionEntityRequestOptionsBuilder<T, R>(this).search(clause);
    }

    public CollectionEntityRequestOptionsBuilder<T, R> expand(String clause) {
        return new CollectionEntityRequestOptionsBuilder<T, R>(this).expand(clause);
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

    public CollectionEntityRequestOptionsBuilder<T, R> metadataFull() {
        return new CollectionEntityRequestOptionsBuilder<T, R>(this).metadataFull();
    }

    public CollectionEntityRequestOptionsBuilder<T, R> metadataMinimal() {
        return new CollectionEntityRequestOptionsBuilder<T, R>(this).metadataMinimal();
    }

    public CollectionEntityRequestOptionsBuilder<T, R> metadataNone() {
        return new CollectionEntityRequestOptionsBuilder<T, R>(this).metadataNone();
    }
    
    public CollectionEntityRequestOptionsBuilder<T, R> urlOverride(String urlOverride) {
        return new CollectionEntityRequestOptionsBuilder<T, R>(this).urlOverride(urlOverride);
    }
    
    public CollectionEntityRequestOptionsBuilder<T, R> connectTimeout(long duration, TimeUnit unit) {
        return new CollectionEntityRequestOptionsBuilder<T, R>(this).connectTimeout(duration, unit);
    }

    public CollectionEntityRequestOptionsBuilder<T, R> readTimeout(long duration, TimeUnit unit) {
        return new CollectionEntityRequestOptionsBuilder<T, R>(this).readTimeout(duration, unit);
    }
    
    public CollectionEntityRequestOptionsBuilder<T, R> deltaTokenLatest() {
        return new CollectionEntityRequestOptionsBuilder<T, R>(this).deltaTokenLatest();
    }

}
