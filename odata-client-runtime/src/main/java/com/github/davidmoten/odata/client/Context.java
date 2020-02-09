package com.github.davidmoten.odata.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class Context {

    private final Serializer serializer;
    private final HttpService service;

    // used to specify options that override standard odata behaviour to deal with
    // service bugs etc. For example, MsGraph @odata.mediaEditLink is wrong for an
    // fileAttachment and needs special editing to get working until Microsoft fix
    // this bug.
    private final Map<String, Object> properties;

    public Context(Serializer serializer, HttpService service, Map<String, Object> properties) {
        this.serializer = serializer;
        this.service = service;
        this.properties = new HashMap<>(properties);
    }
    
    public Context(Serializer serializer, HttpService service) {
        this(serializer, service, Collections.emptyMap());
    }

    public Serializer serializer() {
        return serializer;
    }

    public HttpService service() {
        return service;
    }

    public Object getProperty(String name) {
        return properties.get(name);
    }
    
    public Map<String, Object> getProperties() {
        return new HashMap<>(properties);
    }

}
