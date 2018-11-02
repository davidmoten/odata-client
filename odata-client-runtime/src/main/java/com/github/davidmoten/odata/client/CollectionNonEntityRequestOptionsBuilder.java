package com.github.davidmoten.odata.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.davidmoten.guavamini.Preconditions;

public final class CollectionNonEntityRequestOptionsBuilder<T, R extends NonEntityRequest<T>> {

    private final CollectionPageNonEntityRequest<T, R> request;
    private final List<RequestHeader> requestHeaders = new ArrayList<>();
    private Optional<String> search = Optional.empty();
    private Optional<String> filter = Optional.empty();
    private Optional<String> orderBy = Optional.empty();
    private Optional<Long> skip = Optional.empty();
    private Optional<Long> top = Optional.empty();
    private Optional<String> select = Optional.empty();
    private Optional<String> expand = Optional.empty();
    private String metadata = "minimal";

    CollectionNonEntityRequestOptionsBuilder(CollectionPageNonEntityRequest<T, R> request) {
        this.request = request;
    }

    public CollectionNonEntityRequestOptionsBuilder<T, R> requestHeader(String name, String value) {
        requestHeaders.add(new RequestHeader(name, value));
        return this;
    }

    public CollectionNonEntityRequestOptionsBuilder<T, R> search(String clause) {
        Preconditions.checkNotNull(clause);
        this.search = Optional.of(clause);
        return this;
    }

    public CollectionNonEntityRequestOptionsBuilder<T, R> expand(String clause) {
        Preconditions.checkNotNull(clause);
        this.expand = Optional.of(clause);
        return this;
    }

    public CollectionNonEntityRequestOptionsBuilder<T, R> filter(String clause) {
        Preconditions.checkNotNull(clause);
        this.filter = Optional.of(clause);
        return this;
    }

    public CollectionNonEntityRequestOptionsBuilder<T, R> orderBy(String clause) {
        Preconditions.checkNotNull(clause);
        this.orderBy = Optional.of(clause);
        return this;
    }

    public CollectionNonEntityRequestOptionsBuilder<T, R> skip(long n) {
        Preconditions.checkArgument(n > 0);
        this.skip = Optional.of(n);
        return this;
    }

    public CollectionNonEntityRequestOptionsBuilder<T, R> top(long n) {
        Preconditions.checkArgument(n > 0);
        this.top = Optional.of(n);
        return this;
    }

    public CollectionNonEntityRequestOptionsBuilder<T, R> select(String clause) {
        Preconditions.checkNotNull(clause);
        this.select = Optional.of(clause);
        return this;
    }

    public CollectionNonEntityRequestOptionsBuilder<T, R> metadataFull() {
        this.metadata = "full";
        return this;
    }

    public CollectionNonEntityRequestOptionsBuilder<T, R> metadataMinimal() {
        this.metadata = "minimal";
        return this;
    }

    public CollectionNonEntityRequestOptionsBuilder<T, R> metadataNone() {
        this.metadata = "none";
        return this;
    }

    CollectionNonEntityRequestOptions build() {
        requestHeaders.add(new RequestHeader("Accept", "application/json;odata.metadata=" + metadata));
        return new CollectionNonEntityRequestOptions(requestHeaders, search, filter, orderBy, skip, top, select,
                expand);
    }

    public CollectionPageNonEntity<T> get() {
        return request.get(build());
    }

}
