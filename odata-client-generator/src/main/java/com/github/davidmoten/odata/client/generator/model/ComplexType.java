package com.github.davidmoten.odata.client.generator.model;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.oasisopen.odata.csdl.v4.Schema;
import org.oasisopen.odata.csdl.v4.TComplexType;
import org.oasisopen.odata.csdl.v4.TNavigationProperty;
import org.oasisopen.odata.csdl.v4.TProperty;

import com.github.davidmoten.odata.client.generator.Names;
import com.github.davidmoten.odata.client.generator.Util;

public final class ComplexType extends Structure<TComplexType> {

    public ComplexType(Schema schema, TComplexType c, Names names) {
        super(schema, c, TComplexType.class, names);
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
        return Util.filter(value.getPropertyOrNavigationPropertyOrAnnotation(), TProperty.class)
                .collect(Collectors.toList());
    }

    @Override
    public List<TNavigationProperty> getNavigationProperties() {
        return Util.filter(value.getPropertyOrNavigationPropertyOrAnnotation(),
                TNavigationProperty.class).collect(Collectors.toList());
    }

    @Override
    public Structure<TComplexType> create(Schema schema, TComplexType t) {
        return new ComplexType(schema, t, names);
    }

    @Override
    public boolean isEntityType() {
        return false;
    }

    @Override
    public File getClassFile() {
        return names.getClassFileComplexType(schema(), getName());
    }

    @Override
    public String getSimpleClassName() {
        return names.getSimpleClassNameComplexType(schema(), getName());
    }

    @Override
    public String getPackage() {
        return names.getPackageComplexType(schema());
    }

    public File getDirectoryComplexType() {
        return names.getDirectoryComplexType(schema());
    }

    @Override
    public String getFullType() {
        return names.getFullTypeFromSimpleType(schema(), getName());
    }

    @Override
    public boolean isAbstract() {
        return value.isAbstract();
    }

    @Override
    public File getClassFileCollectionRequest() {
        return names.getClassFileComplexTypeCollectionRequest(schema(), getName());
    }

    public String getFullClassName() {
        return getPackage() + "." + getSimpleClassName();
    }
}
