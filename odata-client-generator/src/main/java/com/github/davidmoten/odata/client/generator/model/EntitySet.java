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
        return Util //
                .filter(container.getEntitySetOrActionImportOrFunctionImport(), TEntitySet.class) //
                .filter(x -> x.getName().equals(name)) //
                .map(es -> new EntitySet(schema, container, es, names)) //
                .findFirst() //
                .orElseGet(() -> names.getSchemas() //
                        .stream() //
                        .flatMap(sch -> Util.types(sch, TEntityContainer.class) //
                                .flatMap(c -> Util
                                        .filter(c.getEntitySetOrActionImportOrFunctionImport(),
                                                TEntitySet.class) //
                                        .filter(es -> name.equals(sch.getNamespace() + "."
                                                + c.getName() + "/" + es.getName()))
                                        .map(es -> new EntitySet(sch, c, es, names))))
                        .findFirst() //
                        .orElseThrow(
                                () -> new RuntimeException("EntitySet " + name + " not found")));
    }

    public String getMethodName(TNavigationPropertyBinding b) {
        return Names.getIdentifier(lastSegment(b.getPath()));
    }
    
    public String getLongerMethodName(TNavigationPropertyBinding b) {
        return Names.getIdentifier(nameFromAllSegments(b.getPath()));
    }

    public String getSimplifiedPath(TNavigationPropertyBinding b) {
        return lastSegment(b.getPath());
    }

    private static String lastSegment(String s) {
        int i = s.lastIndexOf("/");
        if (i == -1) {
            return s;
        } else {
            return s.substring(i + 1);
        }
    }
    
    private static String nameFromAllSegments(String s) {
        return s.replace(".", "_").replace("/", "_");
    }

}
