package com.github.davidmoten.odata.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.github.davidmoten.guavamini.Preconditions;

public final class SingleEntityRequestOptionsBuilder<T extends ODataEntity> {

    private final EntityRequest<T> request;
    private final Map<String, String> requestHeaders = new HashMap<>();
    private Optional<String> select = Optional.empty();
    private Optional<String> expand = Optional.empty();
    private boolean useCaches = false;

    SingleEntityRequestOptionsBuilder(EntityRequest<T> request) {
        this.request = request;
    }

    public SingleEntityRequestOptionsBuilder<T> requestHeader(String key, String value) {
        requestHeaders.put(key, value);
        return this;
    }

    public SingleEntityRequestOptionsBuilder<T> select(String clause) {
        Preconditions.checkNotNull(clause);
        this.select = Optional.of(clause);
        return this;
    }

    public SingleEntityRequestOptionsBuilder<T> expand(String clause) {
        Preconditions.checkNotNull(clause);
        this.expand = Optional.of(clause);
        return this;
    }

    public SingleEntityRequestOptionsBuilder<T> useCaches(boolean value) {
        this.useCaches = value;
        return this;
    }

    public SingleEntityRequestOptionsBuilder<T> useCaches() {
        return useCaches(true);
    }

    public T get() {
        return request.get(build());
    }

    private SingleEntityRequestOptions<T> build() {
        return new SingleEntityRequestOptions<T>(requestHeaders, select, expand);
    }

}
