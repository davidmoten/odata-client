package com.github.davidmoten.odata.client.internal;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ParameterMap {

    public static Builder put(String key, Object value) {
        return new Builder().put(key, value);
    }
    
    public static Map<String, Object> empty() {
        return Collections.emptyMap();
    }

    public static Map<String, Object> build() {
        return new Builder().build();
    }

    public static final class Builder {
        private final Map<String, Object> map = new LinkedHashMap<>();

        public Builder put(String key, Object value) {
            map.put(key, value);
            return this;
        }

        public Map<String, Object> build() {
            return map;
        }

    }

}
