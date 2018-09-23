package com.github.davidmoten.odata.client.generator;

import java.util.List;
import java.util.stream.Collectors;

import org.oasisopen.odata.csdl.v4.TEntityType;
import org.oasisopen.odata.csdl.v4.TNavigationProperty;
import org.oasisopen.odata.csdl.v4.TProperty;

public final class StructureEntityType extends Structure<TEntityType> {

    public StructureEntityType(TEntityType c, Names names) {
        super(c, TEntityType.class, names);
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
        return Util.filter(value.getKeyOrPropertyOrNavigationProperty(), TProperty.class)
                .collect(Collectors.toList());
    }

    @Override
    public List<TNavigationProperty> getNavigationProperties() {
        return Util.filter(value.getKeyOrPropertyOrNavigationProperty(), TNavigationProperty.class)
                .collect(Collectors.toList());
    }

    @Override
    Structure<TEntityType> create(TEntityType t) {
        return new StructureEntityType(t, names);
    }
}
