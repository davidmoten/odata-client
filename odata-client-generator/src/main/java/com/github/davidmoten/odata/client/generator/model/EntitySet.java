package com.github.davidmoten.odata.client.generator.model;

import java.io.File;

import org.oasisopen.odata.csdl.v4.Schema;
import org.oasisopen.odata.csdl.v4.TEntityContainer;
import org.oasisopen.odata.csdl.v4.TEntitySet;
import org.oasisopen.odata.csdl.v4.TNavigationPropertyBinding;

import com.github.davidmoten.odata.client.generator.Imports;
import com.github.davidmoten.odata.client.generator.Names;
import com.github.davidmoten.odata.client.generator.Util;

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

    public String getBaseCollectionRequestClassName(Imports imports) {
        String t = entitySet.getEntityType();
        // an entity set is always a collection
        Schema schema = names.getSchema(t);
        return imports.add(names.getFullClassNameCollectionRequestFromTypeWithNamespace(schema, t));
    }

    public EntitySet getReferredEntitySet(String name) {
        // TODO don't ignore the prefix of the NavigationPropertyBinding target (see
        // odata-client-test-sample-server metadata.xml for example
        TEntitySet t = Util //
                .filter(container.getEntitySetOrActionImportOrFunctionImport(), TEntitySet.class) //
                .filter(x -> x.getName().equals(lastSegment(name))) //
                .findFirst() //
                .orElseThrow(() -> new RuntimeException("EntitySet " + name + " not found"));
        return new EntitySet(schema, container, t, names);
    }

    public String getMethodName(TNavigationPropertyBinding b) {
        return Names.getIdentifier(lastSegment(b.getPath()));
    }

    public String getSimplifiedPath(TNavigationPropertyBinding b) {
        return lastSegment(b.getPath());
    }
    
    private static String lastSegment(String s) {
        int i = s.lastIndexOf("/");
        if (i == -1) {
            return s;
        } else {
            return s.substring(i + 1, s.length());
        }
    }

}
