package com.github.davidmoten.odata.client.generator.model;

import java.util.List;
import java.util.stream.Collectors;

import org.oasisopen.odata.csdl.v4.TComplexType;
import org.oasisopen.odata.csdl.v4.TNavigationProperty;
import org.oasisopen.odata.csdl.v4.TProperty;

import com.github.davidmoten.odata.client.generator.Names;
import com.github.davidmoten.odata.client.generator.Util;

public final class ComplexType extends Structure<TComplexType> {

    public ComplexType(TComplexType c, Names names) {
        super(c, TComplexType.class, names);
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
    public Structure<TComplexType> create(TComplexType t) {
        return new ComplexType(t, names);
    }

    @Override
    public boolean isEntityType() {
        return false;
    }

}
