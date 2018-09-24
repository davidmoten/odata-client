package com.github.davidmoten.odata.client.generator;

final class Field {
    final String name;
    final String fieldName;
    final String propertyName;
    final String importedType;

    Field(String name, String fieldName, String propertyName, String importedType) {
        this.name = name;
        this.fieldName = fieldName;
        this.propertyName = propertyName;
        this.importedType = importedType;
    }
}