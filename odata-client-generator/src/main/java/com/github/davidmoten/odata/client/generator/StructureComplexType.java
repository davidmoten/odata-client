package com.github.davidmoten.odata.client.generator;

import java.util.List;
import java.util.stream.Collectors;

import org.oasisopen.odata.csdl.v4.TComplexType;
import org.oasisopen.odata.csdl.v4.TNavigationProperty;
import org.oasisopen.odata.csdl.v4.TProperty;

public final class StructureComplexType extends Structure<TComplexType> {

    public StructureComplexType(TComplexType c, Names names) {
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
    Structure<TComplexType> create(TComplexType t) {
        return new StructureComplexType(t, names);
    }
}
