package com.github.davidmoten.odata.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.github.davidmoten.guavamini.Preconditions;

final class CollectionRequestOptions implements RequestOptions {

    private final List<RequestHeader> requestHeaders;
    private final Optional<String> search;
    private final Optional<String> filter;
    private final Optional<String> orderBy;
    private final Optional<Long> skip;
    private final Optional<Long> top;
    private final Optional<String> select;
    private final Optional<String> expand;
    private final Optional<String> urlOverride;
    private final Optional<Long> connectTimeoutMs;
    private final Optional<Long> readTimeoutMs;
	private final Optional<String> deltaToken;
	private final Map<String, String> queries;

    CollectionRequestOptions(List<RequestHeader> requestHeaders, Optional<String> search,
            Optional<String> filter, Optional<String> orderBy, Optional<Long> skip,
            Optional<Long> top, Optional<String> select, Optional<String> expand, //
            Optional<String> urlOverride, Optional<Long> connectTimeoutMs, //
            Optional<Long> readTimeoutMs, Optional<String> deltaToken, //
            Map<String, String> queries) {
    	Preconditions.checkNotNull(connectTimeoutMs);
    	Preconditions.checkNotNull(readTimeoutMs);
        this.requestHeaders = requestHeaders;
        this.search = search;
        this.filter = filter;
        this.orderBy = orderBy;
        this.skip = skip;
        this.top = top;
        this.select = select;
        this.expand = expand;
        this.urlOverride = urlOverride;
        this.connectTimeoutMs = connectTimeoutMs;
        this.readTimeoutMs = readTimeoutMs;
        this.deltaToken = deltaToken;
        this.queries = queries;
    }

    @Override
    public List<RequestHeader> getRequestHeaders() {
        return requestHeaders;
    }

    @Override
    public Map<String, String> getQueries() {
        Map<String, String> map = new HashMap<>();
        search.ifPresent(x -> map.put("$search", x));
        filter.ifPresent(x -> map.put("$filter", x));
        orderBy.ifPresent(x -> map.put("$orderBy", x));
        skip.ifPresent(x -> map.put("$skip", String.valueOf(x)));
        top.ifPresent(x -> map.put("$top", String.valueOf(x)));
        select.ifPresent(x -> map.put("$select", x));
        expand.ifPresent(x -> map.put("$expand", x));
        deltaToken.ifPresent(x -> map.put("$deltaToken", x));
        map.putAll(queries);
        return map;
    }

    @Override
    public Optional<String> getUrlOverride() {
        return urlOverride;
    }

	@Override
	public Optional<Long> requestConnectTimeoutMs() {
		return connectTimeoutMs;
	}

	@Override
	public Optional<Long> requestReadTimeoutMs() {
		return readTimeoutMs;
	}
}
