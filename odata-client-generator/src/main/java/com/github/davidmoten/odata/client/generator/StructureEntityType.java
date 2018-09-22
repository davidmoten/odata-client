package com.github.davidmoten.odata.client.generator;

import java.util.stream.Stream;

import org.oasisopen.odata.csdl.v4.TEntityType;
import org.oasisopen.odata.csdl.v4.TProperty;

public class StructureEntityType implements Structure<TEntityType> {

    private final TEntityType c;

    public StructureEntityType(TEntityType c) {
        this.c = c;
    }

    @Override
    public String getName() {
        return c.getName();
    }

    @Override
    public String getBaseType() {
        return c.getBaseType();
    }

    @Override
    public TEntityType value() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Stream<TProperty> getProperties() {
        return Util.filter(c.getKeyOrPropertyOrNavigationProperty(), TProperty.class);
    }

}
