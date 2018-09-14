package com.github.davidmoten.odata.client;

import java.util.ArrayList;
import java.util.List;
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
    public List<String> getQueries() {
        List<String> list = new ArrayList<>();
        select.ifPresent(x -> list.add("$select=" + x));
        expand.ifPresent(x -> list.add("$expand=" + x));
        return list;
    }

}
