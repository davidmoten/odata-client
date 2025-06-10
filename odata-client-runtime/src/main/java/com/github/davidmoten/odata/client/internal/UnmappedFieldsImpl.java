package com.github.davidmoten.odata.client.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.github.davidmoten.odata.client.UnmappedFields;

@SuppressWarnings("ALL")
public final class UnmappedFieldsImpl implements UnmappedFields, Map<String, Object> {

    private final Map<String, Object> map;

    public UnmappedFieldsImpl() {
        this(new HashMap<>());
    }

    public UnmappedFieldsImpl(Map<String, Object> map) {
        this.map = map;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(String key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public Object get(String key) {
        return map.get(key);
    }

    @Override
    public Object put(String key, Object value) {
        return map.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return map.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        map.putAll(m);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<String> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<Object> values() {
        return map.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return map.entrySet();
    }

    @Override
    public String toString() {
        return map.toString();
    }

    public UnmappedFieldsImpl copy() {
        return new UnmappedFieldsImpl(new HashMap<>(map));
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public Object get(Object key) {
        return map.get(key);
    }

}
