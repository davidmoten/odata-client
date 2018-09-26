package com.github.davidmoten.odata.client.generator.model;

public final class Field {
    public final String name;
    public final String fieldName;
    public final String propertyName;
    public final String importedType;

    public Field(String name, String fieldName, String propertyName, String importedType) {
        this.name = name;
        this.fieldName = fieldName;
        this.propertyName = propertyName;
        this.importedType = importedType;
    }
}