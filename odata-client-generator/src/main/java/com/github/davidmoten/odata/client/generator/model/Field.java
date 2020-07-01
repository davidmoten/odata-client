package com.github.davidmoten.odata.client.generator.model;

public final class Field {
    public final String name;
    public final String fieldName;
    public final String propertyName;
    public final String importedType;
    public final boolean isCollection;
    public final String innerFullClassName;

    public Field(String name, String fieldName, String propertyName, String importedType, boolean isCollection, String innerClassName) {
        this.name = name;
        this.fieldName = fieldName;
        this.propertyName = propertyName;
        this.importedType = importedType;
        this.isCollection = isCollection;
        this.innerFullClassName = innerClassName;
    }
}