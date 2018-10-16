package com.github.davidmoten.odata.client;

public final class Context {

    private final Serializer serializer;
    private final HttpService service;

    public Context(Serializer serializer, HttpService service) {
        this.serializer = serializer;
        this.service = service;
    }

    public Serializer serializer() {
        return serializer;
    }

    public HttpService service() {
        return service;
    }

}
