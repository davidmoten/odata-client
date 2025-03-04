package com.github.davidmoten.odata.client.generator.model;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.oasisopen.odata.csdl.v4.Schema;
import org.oasisopen.odata.csdl.v4.TEntityKeyElement;
import org.oasisopen.odata.csdl.v4.TEntityType;
import org.oasisopen.odata.csdl.v4.TNavigationProperty;
import org.oasisopen.odata.csdl.v4.TProperty;

import com.github.davidmoten.odata.client.generator.Names;
import com.github.davidmoten.odata.client.generator.Util;

public final class EntityType extends Structure<TEntityType> {

    public EntityType(Schema schema, TEntityType c, Names names) {
        super(schema, c, TEntityType.class, names);
    }

    @Override
    public String getName() {
        return value.getName();
    }

    @Override
    public String getBaseType() {
        return value.getBaseType();
    }

    @Override
    public List<TProperty> getProperties() {
        return Util //
                .filter(value.getKeyOrPropertyOrNavigationProperty(), TProperty.class) //
                .collect(Collectors.toList());
    }

    @Override
    public List<TNavigationProperty> getNavigationProperties() {
        return Util //
                .filter(value.getKeyOrPropertyOrNavigationProperty(), TNavigationProperty.class)
                .collect(Collectors.toList());
    }

    @Override
    public Structure<TEntityType> create(Schema schema, TEntityType t) {
        return new EntityType(schema, t, names);
    }

    @Override
    public boolean isEntityType() {
        return true;
    }

    @Override
    public String getSimpleClassName() {
        return names.getSimpleClassNameEntity(schema(), value.getName());
    }

    @Override
    public String getFullType() {
        return names.getFullTypeFromSimpleType(schema(), getName());
    }

    @Override
    public File getClassFile() {
        return names.getClassFileEntity(schema(), getName());
    }

    public File getClassFileEntityRequest() {
        return names.getClassFileEntityRequest(schema(), getName());
    }

    @Override
    public File getClassFileCollectionRequest() {
        return names.getClassFileEntityCollectionRequest(schema(), getName());
    }

    @Override
    public String getPackage() {
        return names.getPackageEntity(schema());
    }

    public String getFullClassNameEntity() {
        return names.getFullClassNameFromTypeWithoutNamespace(schema(), getName());
    }

    public String getSimpleClassNameEntityRequest() {
        return names.getSimpleClassNameEntityRequest(schema(), getName());
    }

    public String getPackageEntityRequest() {
        return names.getPackageEntityRequest(schema());
    }

    public File getDirectoryEntity() {
        return names.getDirectoryEntity(schema());
    }

    private List<KeyElement> getKeysLocal() {
        return Util.filter(value.getKeyOrPropertyOrNavigationProperty(), TEntityKeyElement.class) //
                .map(x -> new KeyElement(x, this, names)) //
                .collect(Collectors.toList());
    }

    public List<KeyElement> getKeys() {
        return getHeirarchy() //
                .stream() //
                .flatMap(x -> ((EntityType) x).getKeysLocal().stream()) //
                .collect(Collectors.toList());
    }
    
    public boolean hasKey() {
        return getHeirarchy() //
                .stream() //
                .flatMap(x -> ((EntityType) x).getKeysLocal().stream()) //
                .findAny() //
                .isPresent();
    }

    public Optional<KeyElement> getFirstKey() {
        if (getKeys().isEmpty()) {
            //throw new IllegalStateException("Entity " + getName() + " has no keys!");
            return Optional.empty();
        }
        return Optional.of(getKeys().get(0));
    }

    @Override
    public boolean isAbstract() {
        return value.isAbstract();
    }

    public boolean hasStream() {
        return value.isHasStream();
    }

    public String getFullClassNameEntityRequest() {
        return getPackageEntityRequest() + "." + getSimpleClassNameEntityRequest();
    }

}
