package com.github.davidmoten.odata.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class EntityRequestOptions<T extends ODataEntityType> implements RequestOptions {

    private final List<RequestHeader> requestHeaders;
    private final Map<String, String> queries;
    private final Optional<String> select;
    private final Optional<String> expand;
    private final boolean useCaches;
	private final Optional<Long> connectTimeoutMs;
	private final Optional<Long> readTimeoutMs;

    public EntityRequestOptions(List<RequestHeader> requestHeaders, Optional<String> select,
            Optional<String> expand, boolean useCaches, Optional<Long> connectTimeoutMs, //
            Optional<Long> readTimeoutMs, Map<String, String> queries) {
        this.requestHeaders = requestHeaders;
        this.select = select;
        this.expand = expand;
        this.useCaches = useCaches;
        this.connectTimeoutMs = connectTimeoutMs;
        this.readTimeoutMs = readTimeoutMs;
        this.queries = queries;
    }

    public Optional<String> getSelect() {
        return select;
    }

    public Optional<String> getExpand() {
        return expand;
    }

    public boolean useCaches() {
        return useCaches;
    }

    @Override
    public List<RequestHeader> getRequestHeaders() {
        // TODO include useCaches as header?
        return requestHeaders;
    }

    @Override
    public Map<String, String> getQueries() {
        Map<String, String> map = new HashMap<>();
        select.ifPresent(x -> map.put("$select", x));
        expand.ifPresent(x -> map.put("$expand", x));
        map.putAll(queries);
        return map;
    }

    @Override
    public Optional<String> getUrlOverride() {
        return Optional.empty();
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
