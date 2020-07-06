package com.github.davidmoten.odata.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.github.davidmoten.guavamini.Preconditions;

public final class NonEntityRequestOptionsBuilder<T> {

    private final NonEntityRequest<T> request;
    private final List<RequestHeader> requestHeaders = new ArrayList<>();
    private Optional<String> select = Optional.empty();
    private Optional<String> expand = Optional.empty();
    private boolean useCaches = false;
    private String metadata = "minimal";
    private Optional<Long> connectTimeoutMs = Optional.empty();
    private Optional<Long> readTimeoutMs = Optional.empty();

    NonEntityRequestOptionsBuilder(NonEntityRequest<T> request) {
        this.request = request;
    }

    public NonEntityRequestOptionsBuilder<T> requestHeader(String key, String value) {
        requestHeaders.add(new RequestHeader(key, value));
        return this;
    }

    public NonEntityRequestOptionsBuilder<T> select(String clause) {
        Preconditions.checkNotNull(clause);
        this.select = Optional.of(clause);
        return this;
    }

    public NonEntityRequestOptionsBuilder<T> expand(String clause) {
        Preconditions.checkNotNull(clause);
        this.expand = Optional.of(clause);
        return this;
    }

    public NonEntityRequestOptionsBuilder<T> useCaches(boolean value) {
        // TODO implement useCaches
        this.useCaches = value;
        return this;
    }

    public NonEntityRequestOptionsBuilder<T> ifMatch(String eTag) {
        return requestHeader("If-Match", eTag);
    }

    public NonEntityRequestOptionsBuilder<T> useCaches() {
        return useCaches(true);
    }

    public NonEntityRequestOptionsBuilder<T> metadataNone() {
        this.metadata = "none";
        return this;
    }

    public NonEntityRequestOptionsBuilder<T> metadataMinimal() {
        this.metadata = "minimal";
        return this;
    }
    
    public NonEntityRequestOptionsBuilder<T> metadataFull() {
        this.metadata = "full";
        return this;
    }

    public NonEntityRequestOptionsBuilder<T> connectTimeout(long duration, TimeUnit unit) {
    	this.connectTimeoutMs = Optional.of(unit.toMillis(duration));
    	return this;
    }
    
    public NonEntityRequestOptionsBuilder<T> readTimeout(long duration, TimeUnit unit) {
    	this.readTimeoutMs = Optional.of(unit.toMillis(duration));
    	return this;
    } 

    public T get() {
        return request.get(build());
    }

    private NonEntityRequestOptions<T> build() {
        requestHeaders.add(RequestHeader.acceptJsonWithMetadata(metadata));
        return new NonEntityRequestOptions<T>(requestHeaders, select, expand, useCaches, connectTimeoutMs, readTimeoutMs);
    }

}
