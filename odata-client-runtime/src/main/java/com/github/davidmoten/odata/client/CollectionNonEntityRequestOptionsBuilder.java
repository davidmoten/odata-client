package com.github.davidmoten.odata.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.github.davidmoten.guavamini.Preconditions;

public final class CollectionNonEntityRequestOptionsBuilder<T> {

    private final CollectionPageNonEntityRequest<T> request;
    private final List<RequestHeader> requestHeaders = new ArrayList<>();
    private Optional<String> search = Optional.empty();
    private Optional<String> filter = Optional.empty();
    private Optional<String> orderBy = Optional.empty();
    private Optional<Long> skip = Optional.empty();
    private Optional<Long> top = Optional.empty();
    private Optional<String> select = Optional.empty();
    private Optional<String> expand = Optional.empty();
    private String metadata = "minimal";

    CollectionNonEntityRequestOptionsBuilder(CollectionPageNonEntityRequest<T> request) {
        this.request = request;
    }

    public CollectionNonEntityRequestOptionsBuilder<T> requestHeader(String name, String value) {
        requestHeaders.add(new RequestHeader(name, value));
        return this;
    }

    public CollectionNonEntityRequestOptionsBuilder<T> search(String clause) {
        Preconditions.checkNotNull(clause);
        this.search = Optional.of(clause);
        return this;
    }

    public CollectionNonEntityRequestOptionsBuilder<T> expand(String clause) {
        Preconditions.checkNotNull(clause);
        this.expand = Optional.of(clause);
        return this;
    }

    public CollectionNonEntityRequestOptionsBuilder<T> filter(String clause) {
        Preconditions.checkNotNull(clause);
        this.filter = Optional.of(clause);
        return this;
    }

    public CollectionNonEntityRequestOptionsBuilder<T> orderBy(String clause) {
        Preconditions.checkNotNull(clause);
        this.orderBy = Optional.of(clause);
        return this;
    }

    public CollectionNonEntityRequestOptionsBuilder<T> skip(long n) {
        Preconditions.checkArgument(n > 0);
        this.skip = Optional.of(n);
        return this;
    }

    public CollectionNonEntityRequestOptionsBuilder<T> top(long n) {
        Preconditions.checkArgument(n > 0);
        this.top = Optional.of(n);
        return this;
    }

    public CollectionNonEntityRequestOptionsBuilder<T> select(String clause) {
        Preconditions.checkNotNull(clause);
        this.select = Optional.of(clause);
        return this;
    }

    public CollectionNonEntityRequestOptionsBuilder<T> metadataFull() {
        this.metadata = "full";
        return this;
    }

    public CollectionNonEntityRequestOptionsBuilder<T> metadataMinimal() {
        this.metadata = "minimal";
        return this;
    }

    public CollectionNonEntityRequestOptionsBuilder<T> metadataNone() {
        this.metadata = "none";
        return this;
    }

    CollectionRequestOptions build() {
        requestHeaders.add(RequestHeader.acceptJsonWithMetadata(metadata));
        return new CollectionRequestOptions(requestHeaders, search, filter, orderBy, skip, top, select,
                expand);
    }

    public CollectionPage<T> get() {
        return request.get(build());
    }
    
    public Iterator<T> iterator() {
        return get().iterator();
    }
    
    public Stream<T> stream() {
        return get().stream();
    }

}
