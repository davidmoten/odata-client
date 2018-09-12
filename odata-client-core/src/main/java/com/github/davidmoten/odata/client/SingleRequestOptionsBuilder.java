package com.github.davidmoten.odata.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.github.davidmoten.guavamini.Preconditions;

public final class SingleRequestOptionsBuilder<T extends ODataEntity> {

    private final EntityRequest<T> request;
    private final Map<String, String> requestHeaders = new HashMap<>();
    private Optional<String> select = Optional.empty();
    private Optional<String> expand = Optional.empty();

    SingleRequestOptionsBuilder(EntityRequest<T> request) {
        this.request = request;
    }

    SingleRequestOptionsBuilder<T> requestHeader(String key, String value) {
        requestHeaders.put(key, value);
        return this;
    }

    SingleRequestOptionsBuilder<T> select(String clause) {
        Preconditions.checkNotNull(clause);
        this.select = Optional.of(clause);
        return this;
    }

    SingleRequestOptionsBuilder<T> expand(String clause) {
        Preconditions.checkNotNull(clause);
        this.expand = Optional.of(clause);
        return this;
    }

}
