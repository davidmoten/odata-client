package com.github.davidmoten.odata.client;

import java.util.List;
import java.util.Optional;

import com.github.davidmoten.guavamini.Lists;
import com.github.davidmoten.guavamini.Preconditions;

public final class ActionRequest<T> {
    
    private final List<RequestHeader> requestHeaders;
    private String metadata;
    private boolean useCaches;
    private Optional<String> expand;
    private Optional<String> select;
    
    public ActionRequest(boolean isCollection, Class<?> innerReturnClass, Object... parameters) {
        this.requestHeaders = Lists.newArrayList();
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
        this.select = Optional.of(clause);
        return this;
    }

    public ActionRequest<T> expand(String clause) {
        Preconditions.checkNotNull(clause);
        this.expand = Optional.of(clause);
        return this;
    }

    public ActionRequest<T> useCaches(boolean value) {
        // TODO implement useCaches
        this.useCaches = value;
        return this;
    }

    public ActionRequest<T> useCaches() {
        return useCaches(true);
    }

    public ActionRequest<T> metadataNone() {
        this.metadata = "none";
        return this;
    }

    public ActionRequest<T> metadataMinimal() {
        this.metadata = "minimal";
        return this;
    }

    public ActionRequest<T> metadataFull() {
        this.metadata = "full";
        return this;
    }

    
}
