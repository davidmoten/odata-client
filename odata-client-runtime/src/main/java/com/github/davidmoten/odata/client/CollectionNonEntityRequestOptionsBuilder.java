package com.github.davidmoten.odata.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
    private Optional<String> urlOverride = Optional.empty();

    CollectionNonEntityRequestOptionsBuilder(CollectionPageNonEntityRequest<T> request) {
        this.request = request;
    }

    public CollectionNonEntityRequestOptionsBuilder<T> requestHeader(String name, String value) {
        requestHeaders.add(new RequestHeader(name, value));
        return this;
    }
    
    public CollectionNonEntityRequestOptionsBuilder<T> requestHeader(RequestHeader header) {
        requestHeaders.add(header);
        return this;
    } 
    
    /**
     * Sets the odata.maxpagesize request header value. Is a preference only and may
     * not be honoured by the service.
     * 
     * @param size max page size
     * @return this
     */
    public CollectionNonEntityRequestOptionsBuilder<T> maxPageSize(int size) {
        return requestHeader(RequestHeader.maxPageSize(size));
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
    
    public CollectionNonEntityRequestOptionsBuilder<T> urlOverride(String urlOverride) {
        this.urlOverride = Optional.ofNullable(urlOverride);
        return this;
    }

    CollectionRequestOptions build() {
        requestHeaders.add(RequestHeader.acceptJsonWithMetadata(metadata));
        return new CollectionRequestOptions(requestHeaders, search, filter, orderBy, skip, top,
                select, expand, urlOverride);
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
    
    public List<T> toList() {
        return get().toList();
    }

    public Set<T> toSet() {
        return get().toSet();
    }
}
