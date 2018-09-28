package com.github.davidmoten.odata.client.generator.model;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.oasisopen.odata.csdl.v4.TNavigationProperty;
import org.oasisopen.odata.csdl.v4.TProperty;

import com.github.davidmoten.odata.client.generator.Imports;
import com.github.davidmoten.odata.client.generator.Names;
import com.github.davidmoten.odata.client.generator.Util;

public abstract class Structure<T> {

    protected final T value;
    private final Class<T> cls;
    protected final Names names;

    public Structure(T value, Class<T> cls, Names names) {
        this.value = value;
        this.cls = cls;
        this.names = names;
    }

    public abstract Structure<T> create(T t);

    public abstract String getName();

    public abstract String getBaseType();

    public final boolean hasBaseType() {
        return getBaseType() != null;
    }

    public abstract List<TProperty> getProperties();

    public abstract List<TNavigationProperty> getNavigationProperties();

    public abstract boolean isEntityType();

    public final List<T> getHeirarchy() {
        List<T> a = new LinkedList<>();
        a.add(value);
        Structure<T> st = this;
        while (true) {
            if (st.getBaseType() == null) {
                return a;
            } else {
                String baseTypeSimpleName = names.getSimpleTypeNameFromTypeWithNamespace(st.getBaseType());
                st = names.getSchemas() //
                        .stream() //
                        .flatMap(schema -> Util.types(schema, cls)) //
                        .map(this::create) //
                        .filter(x -> x.getName().equals(baseTypeSimpleName)) //
                        .findFirst() //
                        .get();
                a.add(0, st.value);
            }
        }
    }

    public final List<Field> getFields(Imports imports) {
        List<Field> list = getHeirarchy() //
                .stream() //
                .map(this::create) //
                .flatMap(z -> z.getProperties() //
                        .stream() //
                        .flatMap(x -> toFields(x, imports)))
                .collect(Collectors.toList());
        return list;
    }

    public final List<Field> getFieldsLocal(Imports imports) {
        return getProperties() //
                .stream() //
                .flatMap(x -> toFields(x, imports)) //
                .collect(Collectors.toList());
    }

    private final Stream<Field> toFields(TProperty x, Imports imports) {
        Field a = new Field(x.getName(), Names.getIdentifier(x.getName()), x.getName(),
                names.toImportedType(x, imports));
        if (names.isCollection(x) && !names.isEntityWithNamespace(names.getType(x))) {
            Field b = new Field(x.getName(), Names.getIdentifier(x.getName()) + "NextLink", x.getName() + "@nextLink",
                    imports.add(String.class));
            return Stream.of(a, b);
        } else {
            return Stream.of(a);
        }
    }

    public final List<Field> getSuperFields(Imports imports) {
        List<T> h = getHeirarchy();
        List<Field> list = h.subList(0, h.size() - 1) //
                .stream() //
                .map(this::create) //
                .flatMap(z -> z.getProperties() //
                        .stream() //
                        .flatMap(x -> toFields(x, imports))) //
                .collect(Collectors.toList());
        return list;
    }
}
