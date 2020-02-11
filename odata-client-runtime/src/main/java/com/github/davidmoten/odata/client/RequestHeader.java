package com.github.davidmoten.odata.client;

public final class RequestHeader {

    private final String name;
    private final String value;

    public RequestHeader(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public static RequestHeader create(String name, String value) {
        return new RequestHeader(name, value);
    }

    public String name() {
        return name;
    }

    public String value() {
        return value;
    }

}
