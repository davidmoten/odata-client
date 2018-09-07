package com.github.davidmoten.odata.client;

public final class QueryOption {

    private final String name;
    private final String value;

    public QueryOption(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String name() {
        return name;
    }

    public String value() {
        return value;
    }

}
