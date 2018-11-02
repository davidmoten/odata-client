package com.github.davidmoten.odata.client;

import com.github.davidmoten.odata.client.internal.RequestHelper;

public abstract class NonEntityRequest<T> {

    private final Class<T> cls;
    protected final ContextPath contextPath;
    private final SchemaInfo schemaInfo;

    public NonEntityRequest(Class<T> cls, ContextPath contextPath, SchemaInfo schemaInfo) {
        this.cls = cls;
        this.contextPath = contextPath;
        this.schemaInfo = schemaInfo;
    }

    T get(NonEntityRequestOptions<T> options) {
        return RequestHelper.get(contextPath, cls, options, schemaInfo);
    }

    public T get() {
        return new NonEntityRequestOptionsBuilder<T>(this).get();
    }

    public NonEntityRequestOptionsBuilder<T> requestHeader(String key, String value) {
        return new NonEntityRequestOptionsBuilder<T>(this).requestHeader(key, value);
    }

    public NonEntityRequestOptionsBuilder<T> select(String clause) {
        return new NonEntityRequestOptionsBuilder<T>(this).select(clause);
    }

    public NonEntityRequestOptionsBuilder<T> expand(String clause) {
        return new NonEntityRequestOptionsBuilder<T>(this).expand(clause);
    }

    public NonEntityRequestOptionsBuilder<T> metadataFull() {
        return new NonEntityRequestOptionsBuilder<T>(this).metadataFull();
    }

    public NonEntityRequestOptionsBuilder<T> metadataMinimal() {
        return new NonEntityRequestOptionsBuilder<T>(this).metadataMinimal();
    }

    public NonEntityRequestOptionsBuilder<T> metadataNone() {
        return new NonEntityRequestOptionsBuilder<T>(this).metadataNone();
    }

}
