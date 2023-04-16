package com.github.davidmoten.odata.client.generator.model;

import org.oasisopen.odata.csdl.v4.TProperty;

import com.github.davidmoten.odata.client.generator.Imports;
import com.github.davidmoten.odata.client.generator.Names;
import com.github.davidmoten.odata.client.internal.EdmSchemaInfo;

public final class Property {

    private final TProperty p;
    private final Names names;

    public Property(TProperty p, Names names) {
        this.p = p;
        this.names = names;
    }

    public TProperty getValue() {
        return p;
    }

    public String getImportedType(Imports imports) {
        return names.toImportedFullClassName(p, imports);
    }
    
    public String getFieldName() {
        return Names.getIdentifier(p.getName());
    }

    public String getName() {
        return p.getName();
    }

    public Class<?> getType() {
        return EdmSchemaInfo.INSTANCE.getClassFromTypeWithNamespace(p.getType().get(0));
    }
    
}
