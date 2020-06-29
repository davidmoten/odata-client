package com.github.davidmoten.odata.client;

import java.net.HttpURLConnection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
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

    CollectionPage<T> get(RequestOptions options) {
        ContextPath cp = contextPath.addQueries(options.getQueries());
        final HttpResponse r;
        List<RequestHeader> h = RequestHelper.cleanAndSupplementRequestHeaders(options, "minimal",
                method != HttpMethod.GET);
        if (method == HttpMethod.GET) {
            r = cp.context().service().get(cp.toUrl(), h);
        } else {
            r = cp.context().service().post(cp.toUrl(), h, content.get());
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
                        h);
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
    
    public List<T> toList() {
        return get().toList();
    }
    
    @Override
    public Iterator<T> iterator() {
        return get().iterator();
    }
    
    public CollectionNonEntityRequestOptionsBuilder<T> requestHeader(String key, String value) {
        return new CollectionNonEntityRequestOptionsBuilder<T>(this).requestHeader(key, value);
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

}
