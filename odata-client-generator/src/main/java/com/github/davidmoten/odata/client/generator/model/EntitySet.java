package com.github.davidmoten.odata.client.generator.model;

import java.io.File;
import java.util.Optional;

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

    public Optional<EntitySet> getReferredEntitySet(String name) {
        Optional<EntitySet> es = Util //
                .filter(container.getEntitySetOrActionImportOrFunctionImport(), TEntitySet.class) //
                .filter(x -> x.getName().equals(name)) //
                .map(x -> new EntitySet(schema, container, x, names)) //
                .findFirst();
        if (!es.isPresent()) {
            es = names.getSchemas() //
                    .stream() //
                    .flatMap(sch -> Util //
                            .types(sch, TEntityContainer.class) //
                            .flatMap(c -> Util
                                    .filter(c.getEntitySetOrActionImportOrFunctionImport(),
                                            TEntitySet.class) //
                                    .filter(x -> name.equals(sch.getNamespace() + "." + c.getName()
                                            + "/" + x.getName()))
                                    .map(x -> new EntitySet(sch, c, x, names))))//
                    .findFirst();
        }
        if (!es.isPresent() && names.getOptions(schema).failOnMissingEntitySet) {
            throw new RuntimeException("EntitySet " + name + " not found");
        }
        return es;
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
