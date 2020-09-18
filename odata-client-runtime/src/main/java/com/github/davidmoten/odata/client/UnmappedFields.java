package com.github.davidmoten.odata.client;

import java.util.Collection;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Set;

import com.github.davidmoten.odata.client.internal.UnmappedFieldsImpl;

public interface UnmappedFields {

    public static final UnmappedFieldsImpl EMPTY = new UnmappedFieldsImpl(Collections.emptyMap());

    int size();

    boolean isEmpty();

    boolean containsKey(String key);

    boolean containsValue(Object value);

    Object get(String key);

    Set<String> keySet();

    Collection<Object> values();

    Set<Entry<String, Object>> entrySet();

}
