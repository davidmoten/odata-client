package com.github.davidmoten.odata.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;

import com.github.davidmoten.guavamini.Preconditions;

public final class CollectionNonEntityRequestOptionsBuilder<T> {

    private final CollectionPageNonEntityRequest<T> request;
    private final List<RequestHeader> requestHeaders;
    private final Map<String, String> queries;
    private Optional<String> search;
    private Optional<String> filter;
    private Optional<String> orderBy;
    private Optional<Long> skip;
    private Optional<Long> top;
    private Optional<String> select;
    private Optional<String> expand;
    private String metadata;
    private Optional<String> urlOverride;
    private Optional<Long> connectTimeoutMs;
    private Optional<Long> readTimeoutMs;
    private Optional<String> deltaToken;

    CollectionNonEntityRequestOptionsBuilder(CollectionPageNonEntityRequest<T> request) {
        this(request, //
                new ArrayList<>(), //
                Optional.empty(), //
                Optional.empty(), //
                Optional.empty(), //
                Optional.empty(), //
                Optional.empty(), // 
                Optional.empty(), //
                Optional.empty(), //
                "minimal", //
                Optional.empty(), //
                Optional.empty(), //
                Optional.empty(), //
                Optional.empty(), //
                new HashMap<>());
    }

    private CollectionNonEntityRequestOptionsBuilder(CollectionPageNonEntityRequest<T> request,
            List<RequestHeader> requestHeaders, Optional<String> search, Optional<String> filter,
            Optional<String> orderBy, Optional<Long> skip, Optional<Long> top,
            Optional<String> select, Optional<String> expand, String metadata,
            Optional<String> urlOverride, Optional<Long> connectTimeoutMs, //
            Optional<Long> readTimeoutMs, Optional<String> deltaToken, Map<String, String> queries) {
        this.request = request;
        this.requestHeaders = requestHeaders;
        this.search = search;
        this.filter = filter;
        this.orderBy = orderBy;
        this.skip = skip;
        this.top = top;
        this.select = select;
        this.expand = expand;
        this.metadata = metadata;
        this.urlOverride = urlOverride;
        this.connectTimeoutMs = connectTimeoutMs;
        this.readTimeoutMs = readTimeoutMs;
        this.deltaToken = deltaToken;
        this.queries = queries;
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
    
    public CollectionNonEntityRequestOptionsBuilder<T> connectTimeout(long duration, TimeUnit unit) {
        Preconditions.checkNotNull(unit);
        this.connectTimeoutMs = Optional.of(unit.toMillis(duration));
        return this;
    }
    
    public CollectionNonEntityRequestOptionsBuilder<T> readTimeout(long duration, TimeUnit unit) {
        Preconditions.checkNotNull(unit);
        this.readTimeoutMs = Optional.of(unit.toMillis(duration));
        return this;
    }

    /**
     * Returns a request builder for those members of the collection that are of the
     * requested type. This is referred to in the <a href=
     * "http://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part2-url-conventions.html">OData
     * 4.01 specification</a> as a "restriction to instances of the derived type".
     * 
     * @param <S>
     *            the type ("derived type") to be restricting to
     * @param cls
     *            the Class of the type to restrict to
     * @return a request builder for a collection of instances restricted to the given type
     */
    public <S extends T> CollectionNonEntityRequestOptionsBuilder<S> filter(Class<S> cls) {
        return new CollectionNonEntityRequestOptionsBuilder<S>(request.filter(cls), requestHeaders,
                search, filter, orderBy, skip, top, select, expand, metadata, urlOverride, //
                connectTimeoutMs, readTimeoutMs, deltaToken, queries);
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
    
    public CollectionNonEntityRequestOptionsBuilder<T> deltaTokenLatest() {
        this.deltaToken = Optional.of("latest");
        return this;
    }
    
    public CollectionNonEntityRequestOptionsBuilder<T> query(String name, String value) {
        this.queries.put(name, value);
        return this;
    }

    CollectionRequestOptions build() {
        requestHeaders.add(RequestHeader.acceptJsonWithMetadata(metadata));
        return new CollectionRequestOptions(requestHeaders, search, filter, orderBy, skip, top,
                select, expand, urlOverride, connectTimeoutMs, readTimeoutMs, deltaToken, queries);
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
    
    public <S> S to(Function<? super CollectionPage<T>,? extends S> function) {
    	return function.apply(get());
    }

    public List<T> toList() {
        return get().toList();
    }

    public Set<T> toSet() {
        return get().toSet();
    }

}
