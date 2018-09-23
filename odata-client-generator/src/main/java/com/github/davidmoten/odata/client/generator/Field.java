package com.github.davidmoten.odata.client.generator;

final class Field {
    final String name;
    final String propertyName;
    final String importedType;

    Field(String name, String propertyName, String importedType) {
        this.name = name;
        this.propertyName = propertyName;
        this.importedType = importedType;
    }
}