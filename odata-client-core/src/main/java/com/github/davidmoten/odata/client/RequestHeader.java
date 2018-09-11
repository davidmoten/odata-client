package com.github.davidmoten.odata.client;

public class RequestHeader implements QueryOption {

    private final String key;
    private final String value;

    public RequestHeader(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String key() {
        return key;
    }

    public String value() {
        return value;
    }

}
