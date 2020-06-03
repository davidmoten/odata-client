package com.github.davidmoten.odata.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.github.davidmoten.guavamini.Preconditions;

public final class CollectionEntityRequestOptionsBuilder<T extends ODataEntityType, R extends EntityRequest<T>>
        implements Iterable<T> {

    private final CollectionPageEntityRequest<T, R> request;
    private final List<RequestHeader> requestHeaders = new ArrayList<>();
    private Optional<String> search = Optional.empty();
    private Optional<String> filter = Optional.empty();
    private Optional<String> orderBy = Optional.empty();
    private Optional<Long> skip = Optional.empty();
    private Optional<Long> top = Optional.empty();
    private Optional<String> select = Optional.empty();
    private Optional<String> expand = Optional.empty();
    private Optional<String> entityType = Optional.empty();
    private String metadata = "minimal";

    CollectionEntityRequestOptionsBuilder(CollectionPageEntityRequest<T, R> request) {
        this.request = request;
    }

    public CollectionEntityRequestOptionsBuilder<T, R> requestHeader(String name, String value) {
        requestHeaders.add(new RequestHeader(name, value));
        return this;
    }

    public CollectionEntityRequestOptionsBuilder<T, R> search(String clause) {
        Preconditions.checkNotNull(clause);
        this.search = Optional.of(clause);
        return this;
    }

    public CollectionEntityRequestOptionsBuilder<T, R> expand(String clause) {
        Preconditions.checkNotNull(clause);
        this.expand = Optional.of(clause);
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

    public CollectionEntityRequestOptionsBuilder<T, R> select(String clause) {
        Preconditions.checkNotNull(clause);
        this.select = Optional.of(clause);
        return this;
    }

    public CollectionEntityRequestOptionsBuilder<T, R> entityType(String entityType) {
        this.entityType = Optional.of(entityType);
        return this;
    }

    public CollectionEntityRequestOptionsBuilder<T, R> metadataFull() {
        this.metadata = "full";
        return this;
    }

    public CollectionEntityRequestOptionsBuilder<T, R> metadataMinimal() {
        this.metadata = "minimal";
        return this;
    }

    public CollectionEntityRequestOptionsBuilder<T, R> metadataNone() {
        this.metadata = "none";
        return this;
    }

    CollectionRequestOptions build() {
        requestHeaders.add(RequestHeader.acceptJsonWithMetadata(metadata));
        return new CollectionRequestOptions(requestHeaders, search, filter, orderBy, skip, top,
                select, expand, entityType);
    }

    public CollectionPage<T> get() {
        return request.get(build());
    }

    @Override
    public Iterator<T> iterator() {
        return get().iterator();
    }

    public Stream<T> stream() {
        return get().stream();
    }

    public T post(T entity) {
        return request.post(build(), entity);
    }

}
