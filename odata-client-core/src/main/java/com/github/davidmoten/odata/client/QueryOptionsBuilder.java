package com.github.davidmoten.odata.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.github.davidmoten.guavamini.Preconditions;

public final class QueryOptionsBuilder {

    private final Map<String, String> requestHeaders = new HashMap<>();
    private Optional<String> select = Optional.empty();
    private Optional<String> expand = Optional.empty();
    private Optional<String> search = Optional.empty();
    private Optional<String> filter = Optional.empty();
    private Optional<String> orderBy = Optional.empty();
    private Optional<Long> skip = Optional.empty();
    private Optional<Long> top = Optional.empty();

    QueryOptionsBuilder requestHeader(String key, String value) {
        requestHeaders.put(key, value);
        return this;
    }

    QueryOptionsBuilder2 select(String clause) {
        Preconditions.checkNotNull(clause);
        this.select = Optional.of(clause);
        return new QueryOptionsBuilder2(this);
    }

    QueryOptionsBuilder2 expand(String clause) {
        Preconditions.checkNotNull(clause);
        this.expand = Optional.of(clause);
        return new QueryOptionsBuilder2(this);
    }

    public static final class QueryOptionsBuilder2 {

        private final QueryOptionsBuilder b;

        QueryOptionsBuilder2(QueryOptionsBuilder b) {
            this.b = b;
        }

    }

}
