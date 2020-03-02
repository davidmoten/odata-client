package com.github.davidmoten.odata.client.internal;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class ParameterMap {

    public static Builder put(String key, String typeWithNamespace, Object value) {
        return new Builder().put(key, typeWithNamespace, value);
    }

    public static Map<String, TypedObject> empty() {
        return Collections.emptyMap();
    }

    public static Map<String, TypedObject> build() {
        return new Builder().build();
    }

    public static final class Builder {
        private final Map<String, TypedObject> map = new LinkedHashMap<>();

        public Builder put(String key, String typeWithNamespace, Object value) {
            map.put(key, new TypedObject(typeWithNamespace, value));
            return this;
        }

        public Map<String, TypedObject> build() {
            return map;
        }

    }

    public static Map<String, Object> toMap(Map<String, TypedObject> map) {
        return map //
                .entrySet() //
                .stream() //
                .collect(Collectors.toMap( //
                        entry -> entry.getKey(), //
                        entry -> entry.getValue().object()));
    }

}
