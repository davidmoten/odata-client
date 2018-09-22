package com.github.davidmoten.odata.client.generator;

import java.util.stream.Stream;

import org.oasisopen.odata.csdl.v4.TComplexType;
import org.oasisopen.odata.csdl.v4.TProperty;

public class StructureComplexType implements Structure<TComplexType> {

    private final TComplexType c;

    public StructureComplexType(TComplexType c) {
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
    public TComplexType value() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Stream<TProperty> getProperties() {
        return Util.filter(c.getPropertyOrNavigationPropertyOrAnnotation(), TProperty.class);
    }

}
