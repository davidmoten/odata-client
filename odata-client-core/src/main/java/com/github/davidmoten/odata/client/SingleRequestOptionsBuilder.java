package com.github.davidmoten.odata.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.github.davidmoten.guavamini.Preconditions;

public final class SingleRequestOptionsBuilder {

    private final Map<String, String> requestHeaders = new HashMap<>();
    private Optional<String> select = Optional.empty();
    private Optional<String> expand = Optional.empty();

    SingleRequestOptionsBuilder requestHeader(String key, String value) {
        requestHeaders.put(key, value);
        return this;
    }

    SingleRequestOptionsBuilder2 select(String clause) {
        Preconditions.checkNotNull(clause);
        this.select = Optional.of(clause);
        return new SingleRequestOptionsBuilder2(this);
    }

    SingleRequestOptionsBuilder2 expand(String clause) {
        Preconditions.checkNotNull(clause);
        this.expand = Optional.of(clause);
        return new SingleRequestOptionsBuilder2(this);
    }

    public static final class SingleRequestOptionsBuilder2 {

        private final SingleRequestOptionsBuilder b;

        SingleRequestOptionsBuilder2(SingleRequestOptionsBuilder b) {
            this.b = b;
        }

    }

}
