package com.github.davidmoten.odata.client;

import java.util.Optional;

import com.github.davidmoten.guavamini.Preconditions;

public final class NameValue {

    private final String name;
    private final Object value;

    public NameValue(String name, Object value) {
        Preconditions.checkNotNull(value);
        this.name = name;
        this.value = value;
    }

    public NameValue(String value) {
        this(null, value);
    }

    public Optional<String> name() {
        return Optional.ofNullable(name);
    }

    public String value() {
        return value.toString();
    }

}
