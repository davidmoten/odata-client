package com.github.davidmoten.odata.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.github.davidmoten.guavamini.Preconditions;

public final class CollectionEntityRequestOptionsBuilder<T extends ODataEntity, R extends EntityRequest<T>> {

    private final CollectionPageEntityRequest<T, R> request;
    private final Map<String, String> requestHeaders = new HashMap<>();
    private Optional<String> search = Optional.empty();
    private Optional<String> filter = Optional.empty();
    private Optional<String> orderBy = Optional.empty();
    private Optional<Long> skip = Optional.empty();
    private Optional<Long> top = Optional.empty();

    CollectionEntityRequestOptionsBuilder(CollectionPageEntityRequest<T, R> request) {
        this.request = request;
    }

    public CollectionEntityRequestOptionsBuilder<T, R> requestHeader(String key, String value) {
        requestHeaders.put(key, value);
        return this;
    }

    public CollectionEntityRequestOptionsBuilder<T, R> search(String clause) {
        Preconditions.checkNotNull(clause);
        this.search = Optional.of(clause);
        return this;
    }

    public CollectionEntityRequestOptionsBuilder<T, R> filter(String clause) {
        Preconditions.checkNotNull(clause);
        this.filter = Optional.of(clause);
        return this;
    }

    public CollectionEntityRequestOptionsBuilder<T, R> orderBy(String clause) {
        Preconditions.checkNotNull(clause);
        this.orderBy = Optional.of(clause);
        return this;
    }

    public CollectionEntityRequestOptionsBuilder<T, R> skip(long n) {
        Preconditions.checkArgument(n > 0);
        this.skip = Optional.of(n);
        return this;
    }

    public CollectionEntityRequestOptionsBuilder<T, R> top(long n) {
        Preconditions.checkArgument(n > 0);
        this.top = Optional.of(n);
        return this;
    }

    public CollectionPage<T> get() {
        return request.get(build());
    }

    CollectionEntityRequestOptions build() {
        return new CollectionEntityRequestOptions(requestHeaders, search, filter, orderBy, skip,
                top);
    }
}
