package com.github.davidmoten.odata.client;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface RequestOptions {

    List<RequestHeader> getRequestHeaders();

    Map<String, String> getQueries();
    
    Optional<String> getUrlOverride();

    public static final RequestOptions EMPTY = new RequestOptions() {

        @Override
        public List<RequestHeader> getRequestHeaders() {
            return Collections.emptyList();
        }

        @Override
        public Map<String, String> getQueries() {
            return Collections.emptyMap();
        }

        @Override
        public Optional<String> getUrlOverride() {
            return Optional.empty();
        }

    };
    
    public static RequestOptions create(Map<String, String> queries, List<RequestHeader> requestHeaders) {
        return new RequestOptions() {

            @Override
            public List<RequestHeader> getRequestHeaders() {
                return requestHeaders;
            }

            @Override
            public Map<String, String> getQueries() {
                return queries;
            }

            @Override
            public Optional<String> getUrlOverride() {
                return Optional.empty();
            }
            
        };
    }
    
}
