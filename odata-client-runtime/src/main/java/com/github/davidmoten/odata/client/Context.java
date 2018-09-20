package com.github.davidmoten.odata.client;

public final class Context {

    private final Serializer serializer;
    private final Service service;

    public Context(Serializer serializer, Service service) {
        this.serializer = serializer;
        this.service = service;
    }

    public Serializer serializer() {
        return serializer;
    }

    public Service service() {
        return service;
    }

}
