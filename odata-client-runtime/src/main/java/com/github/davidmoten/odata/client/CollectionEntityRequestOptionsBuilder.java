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
    private Optional<String> search;
    private Optional<String> filter;
    private Optional<String> orderBy;
    private Optional<Long> skip;
    private Optional<Long> top;
    private Optional<String> select;
    private Optional<String> expand;
    private String metadata;

    CollectionEntityRequestOptionsBuilder(CollectionPageEntityRequest<T, R> request) {
        this(request, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), "minimal");
    }
    
    private CollectionEntityRequestOptionsBuilder(CollectionPageEntityRequest<T, R> request, Optional<String> search,
            Optional<String> filter, Optional<String> orderBy, Optional<Long> skip, Optional<Long> top,
            Optional<String> select, Optional<String> expand, String metadata) {
        this.request = request;
        this.search = search;
        this.filter = filter;
        this.orderBy = orderBy;
        this.skip = skip;
        this.top = top;
        this.select = select;
        this.expand = expand;
        this.metadata = metadata;
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
    
    public <S extends T> CollectionEntityRequestOptionsBuilder<S, EntityRequest<S>> filter(Class<S> cls) {
        return new CollectionEntityRequestOptionsBuilder<S, EntityRequest<S>>(request.filter(cls), search, filter,
                orderBy, skip, top, select, expand, metadata);
    }
    
    CollectionRequestOptions build() {
        requestHeaders.add(RequestHeader.acceptJsonWithMetadata(metadata));
        return new CollectionRequestOptions(requestHeaders, search, filter, orderBy, skip, top,
                select, expand);
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

    public List<T> toList() {
        return get().toList();
    }

}
