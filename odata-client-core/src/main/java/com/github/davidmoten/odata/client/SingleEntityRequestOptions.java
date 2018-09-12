package com.github.davidmoten.odata.client;

import java.util.Map;
import java.util.Optional;

public class SingleEntityRequestOptions<T extends ODataEntity> {

    private final Map<String, String> requestHeaders;
    private final Optional<String> select;
    private final Optional<String> expand;
    private final boolean useCaches;

    public SingleEntityRequestOptions(Map<String, String> requestHeaders, Optional<String> select,
            Optional<String> expand, boolean useCaches) {
        this.requestHeaders = requestHeaders;
        this.select = select;
        this.expand = expand;
        this.useCaches = useCaches;
    }

    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
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

}
