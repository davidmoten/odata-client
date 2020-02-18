package com.github.davidmoten.odata.client.internal;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Maps {

    public static <K, V> Builder<K, V> put(K key, V value) {
        return new Builder<K, V>().put(key, value);
    }
    
    public static <K, V> Map<K,V> empty() {
        return Collections.emptyMap();
    }

    public static <K, V> Map<K, V> build() {
        return new Builder<K, V>().build();
    }

    public static final class Builder<K, V> {
        private final Map<K, V> map = new LinkedHashMap<K, V>();

        public Builder<K, V> put(K key, V value) {
            map.put(key, value);
            return this;
        }

        public Map<K, V> build() {
            return map;
        }

    }

}
