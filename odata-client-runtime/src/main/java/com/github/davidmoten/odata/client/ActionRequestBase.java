package com.github.davidmoten.odata.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.davidmoten.guavamini.Preconditions;

public abstract class ActionRequestBase<T extends ActionRequestBase<T>> implements RequestOptions {
    
    protected final ContextPath contextPath;
    
    protected final Map<String, Object> parameters;
    
    protected final List<RequestHeader> requestHeaders = new ArrayList<>();
    protected final Map<String,String> queries = new HashMap<>();

    public ActionRequestBase(Map<String, Object> parameters, ContextPath contextPath) {
        this.parameters = parameters;
        this.contextPath = contextPath;
    }
    
    @SuppressWarnings("unchecked")
    public T requestHeader(String key, String value) {
        requestHeaders.add(new RequestHeader(key, value));
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T select(String clause) {
        Preconditions.checkNotNull(clause);
        queries.put("$select", clause);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T expand(String clause) {
        Preconditions.checkNotNull(clause);
        queries.put("$expand", clause);
        return (T) this;
    }

    public T useCaches() {
        // TODO support useCaches
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    public T metadataNone() {
        requestHeaders.add(RequestHeader.acceptJsonWithMetadata("none"));
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T metadataMinimal() {
        requestHeaders.add(RequestHeader.acceptJsonWithMetadata("minimal"));
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T metadataFull() {
        requestHeaders.add(RequestHeader.acceptJsonWithMetadata("full"));
        return (T) this;
    }

    @Override
    public List<RequestHeader> getRequestHeaders() {
        return requestHeaders;
    }

    @Override
    public Map<String, String> getQueries() {
        return queries;
    }
    
    
}