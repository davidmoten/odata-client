package com.github.davidmoten.odata.client.generator;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.oasisopen.odata.csdl.v4.TComplexType;
import org.oasisopen.odata.csdl.v4.TNavigationProperty;
import org.oasisopen.odata.csdl.v4.TProperty;

public class StructureComplexType implements Structure<TComplexType> {

    private final TComplexType c;
    private final Names names;

    public StructureComplexType(TComplexType c, Names names) {
        this.c = c;
        this.names = names;
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
        return c;
    }

    @Override
    public List<TProperty> getProperties() {
        return Util.filter(c.getPropertyOrNavigationPropertyOrAnnotation(), TProperty.class)
                .collect(Collectors.toList());
    }

    @Override
    public List<TComplexType> getHeirarchy() {
        TComplexType t = value();
        List<TComplexType> a = new LinkedList<>();
        a.add(t);
        while (true) {
            if (t.getBaseType() == null) {
                return a;
            } else {
                TComplexType y = t;
                t = Util.types(names.getSchema(), TComplexType.class) //
                        .filter(x -> x.getName().equals(
                                names.getSimpleTypeNameFromTypeWithNamespace(y.getBaseType()))) //
                        .findFirst() //
                        .get();
                a.add(0, t);
            }
        }
    }

    @Override
    public List<TNavigationProperty> getNavigationProperties() {
        return Util.filter(value().getPropertyOrNavigationPropertyOrAnnotation(),
                TNavigationProperty.class).collect(Collectors.toList());
    }
}
