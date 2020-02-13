package com.github.davidmoten.odata.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.davidmoten.guavamini.Lists;
import com.github.davidmoten.guavamini.Preconditions;

public final class ActionRequest<T> {
    
    private final List<RequestHeader> requestHeaders;
    private final Map<String, String> queries;
    
    public ActionRequest(boolean isCollection, Class<?> innerReturnClass, Object... parameters) {
        this.requestHeaders = Lists.newArrayList();
        this.queries = new HashMap<>();
    }
    
    public T call() {
        //TODO 
        throw new UnsupportedOperationException();
    }
    
    public ActionRequest<T> requestHeader(String key, String value) {
        requestHeaders.add(new RequestHeader(key, value));
        return this;
    }

    public ActionRequest<T> select(String clause) {
        Preconditions.checkNotNull(clause);
        queries.put("$select", clause);
        return this;
    }

    public ActionRequest<T> expand(String clause) {
        Preconditions.checkNotNull(clause);
        queries.put("$expand", clause);
        return this;
    }

    public ActionRequest<T> useCaches() {
        //TODO support useCaches
        throw new UnsupportedOperationException();
    }

    public ActionRequest<T> metadataNone() {
        requestHeaders.add(RequestHeader.acceptJsonWithMetadata("none"));
        return this;
    }

    public ActionRequest<T> metadataMinimal() {
        requestHeaders.add(RequestHeader.acceptJsonWithMetadata("minimal"));
        return this;
    }

    public ActionRequest<T> metadataFull() {
        requestHeaders.add(RequestHeader.acceptJsonWithMetadata("full"));
        return this;
    }
}
