package com.github.davidmoten.odata.client.generator;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.oasisopen.odata.csdl.v4.Schema;
import org.oasisopen.odata.csdl.v4.TAction;
import org.oasisopen.odata.csdl.v4.TActionFunctionParameter;
import org.oasisopen.odata.csdl.v4.TActionFunctionReturnType;
import org.oasisopen.odata.csdl.v4.TAnnotations;
import org.oasisopen.odata.csdl.v4.TComplexType;
import org.oasisopen.odata.csdl.v4.TEntityType;
import org.oasisopen.odata.csdl.v4.TNavigationProperty;
import org.oasisopen.odata.csdl.v4.TProperty;

import com.github.davidmoten.guavamini.Preconditions;

public final class Util {

    static void deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        directoryToBeDeleted.delete();
    }

    public static <T> Stream<T> types(Schema schema, Class<T> cls) {
        return filter(schema.getComplexTypeOrEntityTypeOrTypeDefinition(), cls);
    }

    @SuppressWarnings("unchecked")
    public static <T> Stream<T> filter(Collection<?> c, Class<T> cls) {
        return (Stream<T>) (c.stream() //
                .filter(x -> cls.isInstance(x)));
    }

    public static void rewriteAliases(Schema schema) {
        types(schema, TEntityType.class) //
                .forEach(x -> rewriteAlias(schema, x));
        types(schema, TComplexType.class) //
                .forEach(x -> rewriteAlias(schema, x));
    }

    private static void rewriteAlias(Schema schema, Object x) {
        if (schema.getAlias() == null) {
            return;
        }
        if (x instanceof TEntityType) {
            TEntityType p = (TEntityType) x;
            // mutate types to use alias
            p.setBaseType(rewriteAlias(schema, p.getBaseType()));
            p.getKeyOrPropertyOrNavigationProperty() //
                    .stream() //
                    .forEach(y -> rewriteAlias(schema, y));
        } else if (x instanceof TComplexType) {
            TComplexType p = (TComplexType) x;
            // mutate types to use alias
            p.setBaseType(rewriteAlias(schema, p.getBaseType()));
            p.getPropertyOrNavigationPropertyOrAnnotation() //
                    .stream() //
                    .forEach(y -> rewriteAlias(schema, y));
        } else if (x instanceof TProperty) {
            TProperty p = (TProperty) x;
            rewriteAlias(schema, p.getType());
        } else if (x instanceof TNavigationProperty) {
            TNavigationProperty p = (TNavigationProperty) x;
            rewriteAlias(schema, p.getType());
        } else if (x instanceof TAction) {
            TAction a = (TAction) x;
            a.getParameterOrAnnotationOrReturnType().forEach(y -> rewriteAlias(schema, y));
        } else if (x instanceof TActionFunctionParameter) {
            TActionFunctionParameter p = (TActionFunctionParameter) x;
            rewriteAlias(schema, p.getType());
        } else if (x instanceof TActionFunctionReturnType) {
            TActionFunctionReturnType p = (TActionFunctionReturnType) x;
            rewriteAlias(schema, p.getType());
        } else if (x instanceof TAnnotations) {
            TAnnotations a = (TAnnotations) x;
            a.setTarget(rewriteAlias(schema, a.getTarget()));
        }
    }

    private static void rewriteAlias(Schema schema, List<String> types) {
        List<String> list = types.stream() //
                .map(y -> rewriteAlias(schema, y)) //
                .collect(Collectors.toList());
        types.clear();
        types.addAll(list);
    }

    private static String rewriteAlias(Schema schema, String type) {
        if (type == null || schema.getAlias() == null) {
            return type;
        } else {
            return type.replaceAll("\\b" + schema.getAlias() + "\\b", schema.getNamespace());
        }
    }

}
