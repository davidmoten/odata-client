package com.github.davidmoten.odata.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.github.davidmoten.guavamini.Preconditions;

public final class CollectionRequestOptionsBuilder {

    private final Map<String, String> requestHeaders = new HashMap<>();
    private Optional<String> search = Optional.empty();
    private Optional<String> filter = Optional.empty();
    private Optional<String> orderBy = Optional.empty();
    private Optional<Long> skip = Optional.empty();
    private Optional<Long> top = Optional.empty();

    CollectionRequestOptionsBuilder requestHeader(String key, String value) {
        requestHeaders.put(key, value);
        return this;
    }

    CollectionRequestOptionsBuilder search(String clause) {
        Preconditions.checkNotNull(clause);
        this.search = Optional.of(clause);
        return this;
    }

    CollectionRequestOptionsBuilder filter(String clause) {
        Preconditions.checkNotNull(clause);
        this.filter = Optional.of(clause);
        return this;
    }

    CollectionRequestOptionsBuilder orderBy(String clause) {
        Preconditions.checkNotNull(clause);
        this.orderBy = Optional.of(clause);
        return this;
    }

    CollectionRequestOptionsBuilder skip(long n) {
        Preconditions.checkArgument(n > 0);
        this.skip = Optional.of(n);
        return this;
    }

    CollectionRequestOptionsBuilder top(long n) {
        Preconditions.checkArgument(n > 0);
        this.top = Optional.of(n);
        return this;
    }
}
