package com.github.davidmoten.odata.client.generator.model;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
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

    public abstract boolean isAbstract();

    public abstract List<TProperty> getProperties();

    public final List<Property> getProperties2() {
        return getProperties().stream().map(x -> new Property(x, names)).collect(Collectors.toList());
    }

    public abstract List<TNavigationProperty> getNavigationProperties();

    public abstract boolean isEntityType();

    public final List<? extends Structure<T>> getHeirarchy() {
        List<Structure<T>> a = new LinkedList<>();
        a.add(create(value));
        Structure<T> st = this;
        while (true) {
            if (st.getBaseType() == null) {
                return a;
            } else {
                String baseTypeSimpleName = names.getSimpleTypeNameFromTypeWithNamespace(st.getBaseType());
                // TODO make a map for lookup to increase perf
                st = names.getSchemas() //
                        .stream() //
                        .flatMap(schema -> Util.types(schema, cls)) //
                        .map(this::create) //
                        .filter(x -> x.getName().equals(baseTypeSimpleName)) //
                        .findFirst() //
                        .get();
                a.add(0, st);
            }
        }
    }

    public final List<Field> getFields(Imports imports) {
        List<Field> list = getHeirarchy() //
                .stream() //
                .flatMap(z -> z.getProperties() //
                        .stream() //
                        .flatMap(x -> toFields(x, imports)))
                .collect(Collectors.toList());
        return list;
    }

    public static final class FieldName {
        public final String name;
        public final String fieldName;

        FieldName(String name, String fieldName) {
            this.name = name;
            this.fieldName = fieldName;
        }

    }

    public final List<FieldName> getFieldNames() {
        return getHeirarchy() //
                .stream() //
                .flatMap(z -> z.getProperties() //
                        .stream() //
                        .map(x -> new FieldName(x.getName(), toFieldName(x))))
                .collect(Collectors.toList());
    }

    private String toFieldName(TProperty x) {
        return Names.getIdentifier(x.getName());
    }

    private final Stream<Field> toFields(TProperty x, Imports imports) {
        Field a = new Field(x.getName(), Names.getIdentifier(x.getName()), x.getName(),
                names.toImportedFullClassName(x, imports));
        if (names.isCollection(x) && !names.isEntityWithNamespace(names.getType(x))) {
            Field b = new Field(x.getName(), Names.getIdentifier(x.getName()) + "NextLink", x.getName() + "@nextLink",
                    imports.add(String.class));
            return Stream.of(a, b);
        } else {
            return Stream.of(a);
        }
    }

    public String getExtendsClause(Imports imports) {
        if (getBaseType() != null) {
            return " extends " + imports.add(names.getFullClassNameFromTypeWithNamespace(getBaseType()));
        } else {
            return "";
        }
    }

    public Optional<String> getJavadoc() {
        Stream<String> a = toStream(names.getDocumentation().getDescription(getFullType()).map(Structure::encodeJavadoc));
        Stream<String> b = toStream(names.getDocumentation().getLongDescription(getFullType()).map(Structure::encodeJavadoc));
        return combine(a, b);
    }

    private static Optional<String> combine(Stream<String> a, Stream<String> b) {
        String s = Stream.concat(a, b).collect(Collectors.joining("\n\n<p>"));
        if (s.length() >  0) {
            return Optional.of(s);
        } else {
            return Optional.empty();
        }
    }
    
    public Optional<String> getJavadocProperty(String propertyName) {
        return getFieldNames() //
                .stream() //
                .map(x -> x.name) //
                .filter(x -> x.equals(propertyName)) //
                .findFirst() //
                .flatMap(x -> {
                    Stream<String> a = toStream(names.getDocumentation().getPropertyDescription(getFullType(), propertyName).map(Structure::encodeJavadoc));
                    Stream<String> b = toStream(names.getDocumentation().getPropertyLongDescription(getFullType(), propertyName).map(Structure::encodeJavadoc));
                    return combine(a, b);
                });
    }

    private static String encodeJavadoc(String x) {
        return x.replace("@", "&#064;") //
                .replace("\\", "{@literal \\}") //
                .replace("<", "&lt;") //
                .replace(">", "@gt;");
    }

    private static <T> Stream<T> toStream(Optional<T> o) {
        if (o.isPresent()) {
            return Stream.of(o.get());
        } else {
            return Stream.empty();
        }
    }

    public abstract File getClassFile();

    public abstract String getSimpleClassName();

    public abstract String getPackage();

    public abstract String getFullType();

    public abstract File getClassFileCollectionRequest();

}
