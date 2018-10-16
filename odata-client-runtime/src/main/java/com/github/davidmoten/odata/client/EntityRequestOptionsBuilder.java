package com.github.davidmoten.odata.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.github.davidmoten.guavamini.Preconditions;

public final class EntityRequestOptionsBuilder<T extends ODataEntity> {

    private final EntityRequest<T> request;
    private final Map<String, String> requestHeaders = new HashMap<>();
    private Optional<String> select = Optional.empty();
    private Optional<String> expand = Optional.empty();
    private boolean useCaches = false;

    EntityRequestOptionsBuilder(EntityRequest<T> request) {
        this.request = request;
    }

    public EntityRequestOptionsBuilder<T> requestHeader(String key, String value) {
        requestHeaders.put(key, value);
        return this;
    }

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
        this.useCaches = value;
        return this;
    }

    public EntityRequestOptionsBuilder<T> useCaches() {
        return useCaches(true);
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
        return new EntityRequestOptions<T>(requestHeaders, select, expand, useCaches);
    }


}
