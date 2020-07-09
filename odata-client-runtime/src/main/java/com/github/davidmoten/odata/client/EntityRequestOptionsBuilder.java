package com.github.davidmoten.odata.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.github.davidmoten.guavamini.Preconditions;

public final class EntityRequestOptionsBuilder<T extends ODataEntityType> implements HasSelect<EntityRequestOptionsBuilder<T>> {

    private final EntityRequest<T> request;
    private final List<RequestHeader> requestHeaders = new ArrayList<>();
    private Optional<String> select = Optional.empty();
    private Optional<String> expand = Optional.empty();
    private boolean useCaches = false;
    private String metadata = "minimal";
    private Optional<Long> connectTimeoutMs = Optional.empty();
    private Optional<Long> readTimeoutMs = Optional.empty();

    public EntityRequestOptionsBuilder(EntityRequest<T> request) {
        this.request = request;
    }

    public EntityRequestOptionsBuilder<T> requestHeader(String key, String value) {
        requestHeaders.add(new RequestHeader(key, value));
        return this;
    }

    @Override
    public EntityRequestOptionsBuilder<T> select(String clause) {
        Preconditions.checkNotNull(clause);
        this.select = Optional.of(clause);
        return this;
    }
    
    public EntityRequestOptionsBuilder<T> expand(String clause) {
        Preconditions.checkNotNull(clause);
        this.expand = Optional.of(clause);
        return this;
    }

    public EntityRequestOptionsBuilder<T> useCaches(boolean value) {
        // TODO implement useCaches
        this.useCaches = value;
        return this;
    }

    public EntityRequestOptionsBuilder<T> ifMatch(String eTag) {
        return requestHeader("If-Match", eTag);
    }

    public EntityRequestOptionsBuilder<T> useCaches() {
        return useCaches(true);
    }

    public EntityRequestOptionsBuilder<T> metadataNone() {
        this.metadata = "none";
        return this;
    }

    public EntityRequestOptionsBuilder<T> metadataMinimal() {
        this.metadata = "minimal";
        return this;
    }

    public EntityRequestOptionsBuilder<T> metadataFull() {
        this.metadata = "full";
        return this;
    }
    
    public EntityRequestOptionsBuilder<T> connectTimeout(long duration, TimeUnit unit) {
        this.connectTimeoutMs = Optional.of(unit.toMillis(duration));
        return this;
    }

    public EntityRequestOptionsBuilder<T> readTimeout(long duration, TimeUnit unit) {
        this.readTimeoutMs = Optional.of(unit.toMillis(duration));
        return this;
    }
    
    public T get() {
        return request.get(build());
    }

    public T patch(T entity) {
        return request.patch(build(), entity);
    }

    public T put(T entity) {
        return request.put(build(), entity);
    }

    public void delete() {
        request.delete(build());
    }

    private EntityRequestOptions<T> build() {
        requestHeaders.add(RequestHeader.acceptJsonWithMetadata(metadata));
        return new EntityRequestOptions<T>(requestHeaders, select, expand, useCaches, connectTimeoutMs, readTimeoutMs);
    }

}
