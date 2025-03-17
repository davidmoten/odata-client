package com.github.davidmoten.odata.client.generator.model;

import java.util.HashMap;
import java.util.Map;

import org.oasisopen.odata.csdl.v4.TProperty;
import org.oasisopen.odata.csdl.v4.TPropertyRef;

import com.github.davidmoten.odata.client.generator.Names;

public class PropertyRef {

    private final TPropertyRef value;
    private final EntityType entityType;
    private final Names names;
    
    public PropertyRef(TPropertyRef value, EntityType entityType, Names names) {
        this.value = value;
        this.entityType = entityType;
        this.names = names;
    }

    public String getName() {
        return value.getName();
    }

    public String getAlias() {
        return value.getAlias();
    }

    public String getFieldName() {
        return Names.getIdentifier(value.getName());
    }

    public Property getReferredProperty() {
        return names.property(entityType, getName());
    }
}
