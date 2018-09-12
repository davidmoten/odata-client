package com.github.davidmoten.odata.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.github.davidmoten.guavamini.Preconditions;

final class CollectionRequestOptionsBuilder<T extends ODataEntity, R extends EntityRequest<T>> {

    private final CollectionPageEntityRequest<T, R> request;
    private final Map<String, String> requestHeaders = new HashMap<>();
    private Optional<String> search = Optional.empty();
    private Optional<String> filter = Optional.empty();
    private Optional<String> orderBy = Optional.empty();
    private Optional<Long> skip = Optional.empty();
    private Optional<Long> top = Optional.empty();

    CollectionRequestOptionsBuilder(CollectionPageEntityRequest<T, R> request) {
        this.request = request;
    }

    CollectionRequestOptionsBuilder<T, R> requestHeader(String key, String value) {
        requestHeaders.put(key, value);
        return this;
    }

    CollectionRequestOptionsBuilder<T, R> search(String clause) {
        Preconditions.checkNotNull(clause);
        this.search = Optional.of(clause);
        return this;
    }

    CollectionRequestOptionsBuilder<T, R> filter(String clause) {
        Preconditions.checkNotNull(clause);
        this.filter = Optional.of(clause);
        return this;
    }

    CollectionRequestOptionsBuilder<T, R> orderBy(String clause) {
        Preconditions.checkNotNull(clause);
        this.orderBy = Optional.of(clause);
        return this;
    }

    CollectionRequestOptionsBuilder<T, R> skip(long n) {
        Preconditions.checkArgument(n > 0);
        this.skip = Optional.of(n);
        return this;
    }

    CollectionRequestOptionsBuilder<T, R> top(long n) {
        Preconditions.checkArgument(n > 0);
        this.top = Optional.of(n);
        return this;
    }

    CollectionRequestOptions build() {
        return new CollectionRequestOptions(requestHeaders, search, filter, orderBy, skip, top);
    }
}
