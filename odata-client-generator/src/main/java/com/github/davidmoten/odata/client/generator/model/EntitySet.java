package com.github.davidmoten.odata.client.generator.model;

import java.io.File;

import org.oasisopen.odata.csdl.v4.Schema;
import org.oasisopen.odata.csdl.v4.TEntityContainer;
import org.oasisopen.odata.csdl.v4.TEntitySet;

import com.github.davidmoten.odata.client.generator.Names;

public final class EntitySet {

    private final Schema schema;
    private final TEntityContainer container;
    private final TEntitySet entitySet;
    private final Names names;

    public EntitySet(Schema schema, TEntityContainer container, TEntitySet entitySet, Names names) {
        this.schema = schema;
        this.container = container;
        this.entitySet = entitySet;
        this.names = names;
    }

    public String getFullClassNameEntitySet() {
        return getPackage() + "." + getSimpleClassNameEntitySet();
    }

    public String getPackage() {
        return names.getPackageEntitySet(schema);
    }

    public String getSimpleClassNameEntitySet() {
        return Names.toSimpleClassName(entitySet.getName());
    }

    public File getClassFile() {
        return names.getClassFileEntitySet(schema, entitySet.getName());
    }

    public File getDirectoryEntitySet() {
        return names.getDirectoryEntitySet(schema);
    }

}
