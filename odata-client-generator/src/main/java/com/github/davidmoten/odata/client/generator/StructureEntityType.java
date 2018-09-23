package com.github.davidmoten.odata.client.generator;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.oasisopen.odata.csdl.v4.TEntityType;
import org.oasisopen.odata.csdl.v4.TNavigationProperty;
import org.oasisopen.odata.csdl.v4.TProperty;

public class StructureEntityType implements Structure<TEntityType> {

    private final TEntityType c;
    private final Names names;

    public StructureEntityType(TEntityType c, Names names) {
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
    public TEntityType value() {
        return c;
    }

    @Override
    public List<TProperty> getProperties() {
        return Util.filter(c.getKeyOrPropertyOrNavigationProperty(), TProperty.class)
                .collect(Collectors.toList());
    }

    @Override
    public List<TNavigationProperty> getNavigationProperties() {
        return Util
                .filter(value().getKeyOrPropertyOrNavigationProperty(), TNavigationProperty.class)
                .collect(Collectors.toList());
    }

    @Override
    public List<TEntityType> getHeirarchy() {
        List<TEntityType> a = new LinkedList<>();
        TEntityType t = value();
        a.add(t);
        while (true) {
            if (t.getBaseType() == null) {
                return a;
            } else {
                TEntityType y = t;
                t = Util.types(names.getSchema(), TEntityType.class) //
                        .filter(x -> x.getName().equals(
                                names.getSimpleTypeNameFromTypeWithNamespace(y.getBaseType()))) //
                        .findFirst() //
                        .get();
                a.add(0, t);
            }
        }
    }

}
