package com.github.davidmoten.odata.client;

import java.util.Collections;
import java.util.Map;

public interface RequestOptions {

    Map<String, String> getRequestHeaders();

    Map<String, String> getQueries();

    public static final RequestOptions EMPTY = new RequestOptions() {

        @Override
        public Map<String, String> getRequestHeaders() {
            return Collections.emptyMap();
        }

        @Override
        public Map<String, String> getQueries() {
            return Collections.emptyMap();
        }

    };

}
