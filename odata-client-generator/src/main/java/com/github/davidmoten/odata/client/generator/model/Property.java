package com.github.davidmoten.odata.client.generator.model;

import org.oasisopen.odata.csdl.v4.TProperty;

import com.github.davidmoten.odata.client.generator.Imports;
import com.github.davidmoten.odata.client.generator.Names;

public final class Property {

    private final TProperty p;
    private final String fieldName;
    private final Names names;

    public Property(TProperty p, String fieldName, Names names) {
        this.p = p;
        this.fieldName = fieldName;
        this.names = names;
    }

    public TProperty getValue() {
        return p;
    }

    public String getImportedType(Imports imports) {
        return names.toImportedFullClassName(p, imports);
    }
    
    public String getFieldName() {
        return fieldName;
    }

    public String getName() {
        return p.getName();
    }

}
