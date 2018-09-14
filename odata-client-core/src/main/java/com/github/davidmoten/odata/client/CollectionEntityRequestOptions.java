package com.github.davidmoten.odata.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class CollectionEntityRequestOptions implements RequestOptions {

    private final Map<String, String> requestHeaders;
    private final Optional<String> search;
    private final Optional<String> filter;
    private final Optional<String> orderBy;
    private final Optional<Long> skip;
    private final Optional<Long> top;

    public CollectionEntityRequestOptions(Map<String, String> requestHeaders, Optional<String> search,
            Optional<String> filter, Optional<String> orderBy, Optional<Long> skip, Optional<Long> top) {
        this.requestHeaders = requestHeaders;
        this.search = search;
        this.filter = filter;
        this.orderBy = orderBy;
        this.skip = skip;
        this.top = top;
    }

    @Override
    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    @Override
    public List<String> getQueries() {
        List<String> list = new ArrayList<>();
        search.ifPresent(x -> list.add("$search=" + x));
        filter.ifPresent(x -> list.add("$filter=" + x));
        orderBy.ifPresent(x -> list.add("$orderBy=" + x));
        skip.ifPresent(x -> list.add("$skip=" + x));
        top.ifPresent(x -> list.add("$top=" + x));
        return list;
    }

    public Optional<String> getSearch() {
        return search;
    }

    public Optional<String> getFilter() {
        return filter;
    }

    public Optional<String> getOrderBy() {
        return orderBy;
    }

    public Optional<Long> getSkip() {
        return skip;
    }

    public Optional<Long> getTop() {
        return top;
    }
}
