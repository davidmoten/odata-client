package com.github.davidmoten.odata.client.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

public final class TypedObject {

    private final String typeWithNamespace;
    
    @JsonProperty("object")
    @JsonValue
    private final Object object;

    public TypedObject(String typeWithNamespace, Object object) {
        this.typeWithNamespace = typeWithNamespace;
        this.object = object;
    }

    public String typeWithNamespace() {
        return typeWithNamespace;
    }

    public Object object() {
        return object;
    }
    
}
