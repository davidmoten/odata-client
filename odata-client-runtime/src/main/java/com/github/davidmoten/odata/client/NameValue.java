package com.github.davidmoten.odata.client;

import java.util.Optional;

import com.github.davidmoten.guavamini.Preconditions;

public final class NameValue {

    private final String name;
    private final Object value;
    private final Class<?> cls;

    public NameValue(String name, Object value, Class<?> cls) {
        Preconditions.checkNotNull(value);
        this.name = name;
        this.value = value;
        this.cls = cls;
    }

    public NameValue(Object value, Class<?> cls) {
        this(null, value, cls);
    }

    public Optional<String> name() {
        return Optional.ofNullable(name);
    }

    public Object value() {
        return value;
    }
    
    public Class<?> cls() {
        return cls;
    }

}
