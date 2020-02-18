package com.github.davidmoten.odata.client;

import java.net.HttpURLConnection;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.github.davidmoten.guavamini.Preconditions;

@JsonIgnoreType
public class CollectionPageNonEntityRequest<T> {

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

    CollectionPage<T> get(RequestOptions options) {
        ContextPath cp = contextPath.addQueries(options.getQueries());
        final HttpResponse r;
        if (method == HttpMethod.GET) {
            r = cp.context().service().get(cp.toUrl(), options.getRequestHeaders());
        } else {
            r = cp.context().service().post(cp.toUrl(), options.getRequestHeaders(), content.get());
        }
        return cp //
                .context() //
                .serializer() //
                .deserializeCollectionPageNonEntity( //
                        r.getText(expectedResponseCode), //
                        cls, //
                        cp, //
                        schemaInfo);
    }

    public CollectionPage<T> get() {
        return new CollectionNonEntityRequestOptionsBuilder<T>(this).get();
    }

    public CollectionNonEntityRequestOptionsBuilder<T> requestHeader(String key, String value) {
        return new CollectionNonEntityRequestOptionsBuilder<T>(this).requestHeader(key, value);
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

}
