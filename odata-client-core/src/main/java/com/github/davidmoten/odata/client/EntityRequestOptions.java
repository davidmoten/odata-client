package com.github.davidmoten.odata.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class EntityRequestOptions<T extends ODataEntity> implements RequestOptions {

    private final Map<String, String> requestHeaders;
    private final Optional<String> select;
    private final Optional<String> expand;
    private final boolean useCaches;

    public EntityRequestOptions(Map<String, String> requestHeaders, Optional<String> select, Optional<String> expand,
            boolean useCaches) {
        this.requestHeaders = requestHeaders;
        this.select = select;
        this.expand = expand;
        this.useCaches = useCaches;
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
    public Map<String, String> getRequestHeaders() {
        // TODO include useCaches as header?
        return requestHeaders;
    }

    @Override
    public Map<String, String> getQueries() {
        Map<String, String> map = new HashMap<>();
        select.ifPresent(x -> map.put("$select", x));
        expand.ifPresent(x -> map.put("$expand", x));
        return map;
    }

}
