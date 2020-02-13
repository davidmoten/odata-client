package com.github.davidmoten.odata.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.davidmoten.guavamini.Preconditions;

public abstract class ActionRequestBase<T extends ActionRequestBase<T>> {
    
    protected final List<RequestHeader> requestHeaders = new ArrayList<>();
    protected final Map<String,String> queries = new HashMap<>();

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
}