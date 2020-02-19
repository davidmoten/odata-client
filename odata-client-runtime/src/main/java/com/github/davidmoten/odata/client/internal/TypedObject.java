package com.github.davidmoten.odata.client.internal;

public final class TypedObject {
    
    private final String typeWithNamespace;
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
