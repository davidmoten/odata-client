package com.github.davidmoten.odata.client;

import static com.github.davidmoten.odata.client.internal.Util.odataTypeNameFromAny;

import java.net.HttpURLConnection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.odata.client.internal.RequestHelper;
import com.github.davidmoten.odata.client.internal.TypedObject;

@JsonIgnoreType
public class CollectionPageNonEntityRequest<T> implements Iterable<T> {

    private final ContextPath contextPath;
    private final Class<T> cls;
    private final SchemaInfo schemaInfo;

    // initial call made with this method, further pages use HttpMethod.GET
    private final HttpMethod method;
    private final Optional<String> content;
    private final int expectedResponseCode;

    // should not be public api
    public CollectionPageNonEntityRequest(ContextPath contextPath, Class<T> cls,
            SchemaInfo schemaInfo, HttpMethod method, Optional<String> content,
            int expectedResponseCode) {
        Preconditions.checkArgument(method != HttpMethod.POST || content.isPresent());
        this.contextPath = contextPath;
        this.cls = cls;
        this.schemaInfo = schemaInfo;
        this.method = method;
        this.content = content;
        this.expectedResponseCode = expectedResponseCode;
    }

    public CollectionPageNonEntityRequest(ContextPath contextPath, Class<T> cls,
            SchemaInfo schemaInfo) {
        this(contextPath, cls, schemaInfo, HttpMethod.GET, Optional.empty(),
                HttpURLConnection.HTTP_CREATED);
    }
    
    public static <T> CollectionPageNonEntityRequest<T> forAction(ContextPath contextPath,
            Class<T> returnClass, Map<String, TypedObject> parameters,
            SchemaInfo returnTypeSchemaInfo) {
        String json = contextPath.context().serializer().serialize(parameters);
        return new CollectionPageNonEntityRequest<T>( //
                contextPath, //
                returnClass, //
                returnTypeSchemaInfo, //
                HttpMethod.POST, //
                Optional.of(json), //
                HttpURLConnection.HTTP_OK);
    }

    public static <T> CollectionPageNonEntityRequest<T> forFunction(ContextPath contextPath,
            Class<T> returnClass, Map<String, TypedObject> parameters,
            SchemaInfo returnTypeSchemaInfo) {
        String json = contextPath.context().serializer().serialize(parameters);
        return new CollectionPageNonEntityRequest<T>( //
                contextPath, //
                returnClass, //
                returnTypeSchemaInfo, //
                HttpMethod.GET, //
                Optional.of(json), //
                HttpURLConnection.HTTP_OK);
    }
    
    CollectionPage<T> get(RequestOptions options) {
        ContextPath cp = contextPath.addQueries(options.getQueries());
        final HttpResponse r;
        List<RequestHeader> h = RequestHelper.cleanAndSupplementRequestHeaders(options, "minimal",
                method != HttpMethod.GET);
        if (method == HttpMethod.GET) {
            r = cp.context().service().get(cp.toUrl(), h, options);
        } else {
            r = cp.context().service().post(cp.toUrl(), h, content.get(), options);
        }
        RequestHelper.checkResponseCode(cp, r, expectedResponseCode);
        return cp //
                .context() //
                .serializer() //
                .deserializeCollectionPage( //
                        r.getText(), //
                        cls, //
                        cp, //
                        schemaInfo, //
                        h, //
                        options, //
                        null);
    }
    
    public CollectionPage<T> get() {
        return new CollectionNonEntityRequestOptionsBuilder<T>(this).get();
    }
    
    public void forEach(Consumer<? super T> consumer) {
        stream().forEach(consumer);
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
    
    @Override
    public Iterator<T> iterator() {
        return get().iterator();
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
    public <S extends T> CollectionPageNonEntityRequest<S> filter(Class<S> cls) {
        return new CollectionPageNonEntityRequest<S>( //
                contextPath.addSegment(odataTypeNameFromAny(cls)), cls, schemaInfo);
    }

    public CollectionNonEntityRequestOptionsBuilder<T> requestHeader(String key, String value) {
        return new CollectionNonEntityRequestOptionsBuilder<T>(this).requestHeader(key, value);
    }
    
    public CollectionNonEntityRequestOptionsBuilder<T> query(String name, String value) {
        return new CollectionNonEntityRequestOptionsBuilder<T>(this).query(name, value);
    }
    
    public CollectionNonEntityRequestOptionsBuilder<T> requestHeader(RequestHeader header) {
        return new CollectionNonEntityRequestOptionsBuilder<T>(this).requestHeader(header);
    }
    
    public CollectionNonEntityRequestOptionsBuilder<T> maxPageSize(int maxPageSize) {
        return new CollectionNonEntityRequestOptionsBuilder<T>(this).maxPageSize(maxPageSize);
    }
    
    public CollectionNonEntityRequestOptionsBuilder<T> search(String clause) {
        return new CollectionNonEntityRequestOptionsBuilder<T>(this).search(clause);
    }

    public CollectionNonEntityRequestOptionsBuilder<T> filter(String clause) {
        return new CollectionNonEntityRequestOptionsBuilder<T>(this).filter(clause);
    }

    public CollectionNonEntityRequestOptionsBuilder<T> orderBy(String clause) {
        return new CollectionNonEntityRequestOptionsBuilder<T>(this).orderBy(clause);
    }

    public CollectionNonEntityRequestOptionsBuilder<T> skip(long n) {
        return new CollectionNonEntityRequestOptionsBuilder<T>(this).skip(n);
    }

    public CollectionNonEntityRequestOptionsBuilder<T> top(long n) {
        return new CollectionNonEntityRequestOptionsBuilder<T>(this).top(n);
    }

    public CollectionNonEntityRequestOptionsBuilder<T> select(String clause) {
        return new CollectionNonEntityRequestOptionsBuilder<T>(this).select(clause);
    }

    public CollectionNonEntityRequestOptionsBuilder<T> metadataFull() {
        return new CollectionNonEntityRequestOptionsBuilder<T>(this).metadataFull();
    }

    public CollectionNonEntityRequestOptionsBuilder<T> metadataMinimal() {
        return new CollectionNonEntityRequestOptionsBuilder<T>(this).metadataMinimal();
    }

    public CollectionNonEntityRequestOptionsBuilder<T> metadataNone() {
        return new CollectionNonEntityRequestOptionsBuilder<T>(this).metadataNone();
    }
    
    public CollectionNonEntityRequestOptionsBuilder<T> urlOverride(String urlOverride) {
        return new CollectionNonEntityRequestOptionsBuilder<T>(this).urlOverride(urlOverride);
    }

    public CollectionNonEntityRequestOptionsBuilder<T> deltaTokenLatest() {
        return new CollectionNonEntityRequestOptionsBuilder<T>(this).deltaTokenLatest();
    }
}
