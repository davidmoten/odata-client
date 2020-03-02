package com.github.davidmoten.odata.client.generator;

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
import org.oasisopen.odata.csdl.v4.TFunction;
import org.oasisopen.odata.csdl.v4.TNavigationProperty;
import org.oasisopen.odata.csdl.v4.TProperty;

import com.github.davidmoten.guavamini.annotations.VisibleForTesting;

public final class Util {

    public static <T> Stream<T> types(Schema schema, Class<T> cls) {
        return filter(schema.getComplexTypeOrEntityTypeOrTypeDefinition(), cls);
    }

    @SuppressWarnings("unchecked")
    public static <T> Stream<T> filter(Collection<?> c, Class<T> cls) {
        return (Stream<T>) (c.stream() //
                .filter(x -> cls.isInstance(x)));
    }
    
    static void replaceAliases(List<Schema> schemas) {
        for (Schema schema: schemas) {
            for (Schema aliasedSchema: schemas) {
                replaceAliases(schema, aliasedSchema);
            }
        }
    }

    static void replaceAliases(Schema schema, Schema aliasedSchema) {
        types(schema, TEntityType.class) //
                .forEach(x -> replaceAlias(aliasedSchema, x));
        types(schema, TComplexType.class) //
                .forEach(x -> replaceAlias(aliasedSchema, x));
        types(schema, TAction.class) //
                .forEach(x -> replaceAlias(aliasedSchema, x));
        types(schema, TFunction.class) //
                .forEach(x -> replaceAlias(aliasedSchema, x));
    }

    private static void replaceAlias(Schema schema, Object x) {
        if (schema.getAlias() == null) {
            return;
        }
        if (x instanceof TEntityType) {
            TEntityType p = (TEntityType) x;
            // mutate types to use alias
            p.setBaseType(replaceAlias(schema, p.getBaseType()));
            p.getKeyOrPropertyOrNavigationProperty() //
                    .stream() //
                    .forEach(y -> replaceAlias(schema, y));
        } else if (x instanceof TComplexType) {
            TComplexType p = (TComplexType) x;
            // mutate types to use alias
            p.setBaseType(replaceAlias(schema, p.getBaseType()));
            p.getPropertyOrNavigationPropertyOrAnnotation() //
                    .stream() //
                    .forEach(y -> replaceAlias(schema, y));
        } else if (x instanceof TAction) {
            TAction a = (TAction) x;
            a.getParameterOrAnnotationOrReturnType().stream() //
                    .forEach(y -> replaceAlias(schema, y));
        } else if (x instanceof TFunction) {
            TFunction a = (TFunction) x;
            a.getParameterOrAnnotation().stream() //
                    .forEach(y -> replaceAlias(schema, y));
            replaceAlias(schema, a.getReturnType());
        } else if (x instanceof TActionFunctionParameter) {
            TActionFunctionParameter p = (TActionFunctionParameter) x;
            replaceAlias(schema, p.getType());
        } else if (x instanceof TActionFunctionReturnType) {
            TActionFunctionReturnType p = (TActionFunctionReturnType) x;
            replaceAlias(schema, p.getType());
        } else if (x instanceof TProperty) {
            TProperty p = (TProperty) x;
            replaceAlias(schema, p.getType());
        } else if (x instanceof TNavigationProperty) {
            TNavigationProperty p = (TNavigationProperty) x;
            replaceAlias(schema, p.getType());
        } else if (x instanceof TAction) {
            TAction a = (TAction) x;
            a.getParameterOrAnnotationOrReturnType().forEach(y -> replaceAlias(schema, y));
        } else if (x instanceof TActionFunctionParameter) {
            TActionFunctionParameter p = (TActionFunctionParameter) x;
            replaceAlias(schema, p.getType());
        } else if (x instanceof TActionFunctionReturnType) {
            TActionFunctionReturnType p = (TActionFunctionReturnType) x;
            replaceAlias(schema, p.getType());
        } else if (x instanceof TAnnotations) {
            TAnnotations a = (TAnnotations) x;
            a.setTarget(replaceAlias(schema, a.getTarget()));
        }
    }

    private static void replaceAlias(Schema schema, List<String> types) {
        List<String> list = types.stream() //
                .map(y -> replaceAlias(schema, y)) //
                .collect(Collectors.toList());
        types.clear();
        types.addAll(list);
    }

    private static String replaceAlias(Schema schema, String type) {
        return replaceAlias(schema.getAlias(), schema.getNamespace(), type);
    }
    
    @VisibleForTesting
    static String replaceAlias(String alias, String namespace, String type) {
        if (type == null || alias == null) {
            return type;
        } if (type.startsWith("Collection(")) {
            return type.replaceFirst("^Collection\\(" + alias + "\\b", "Collection(" + namespace);
        } else {
            return type.replaceFirst("^" + alias + "\\b", namespace);
        }
    }
    
}
