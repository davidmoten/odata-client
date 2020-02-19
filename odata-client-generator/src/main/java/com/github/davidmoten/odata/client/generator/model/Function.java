package com.github.davidmoten.odata.client.generator.model;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.oasisopen.odata.csdl.v4.Schema;
import org.oasisopen.odata.csdl.v4.TActionFunctionParameter;
import org.oasisopen.odata.csdl.v4.TActionFunctionReturnType;
import org.oasisopen.odata.csdl.v4.TFunction;

import com.github.davidmoten.odata.client.generator.Imports;
import com.github.davidmoten.odata.client.generator.Names;
import com.github.davidmoten.odata.client.generator.Util;
import com.github.davidmoten.odata.client.internal.EdmSchemaInfo;

public final class Function implements Method {

    private TFunction function;
    private Names names;

    public Function(TFunction function, Names names) {
        this.function = function;
        this.names = names;
    }

    public Schema schema() {
        return names.getSchema(function);
    }

    public String getFullClassNameActionReturnType() {
        Optional<TActionFunctionReturnType> returnParameter = Util
                .filter(function.getParameterOrAnnotation(), TActionFunctionReturnType.class)
                .findFirst();
        if (!returnParameter.isPresent()) {
            return Void.class.getName();
        } else {
            return names.getFullClassNameFromTypeWithNamespace(
                    names.getInnerType(returnParameter.get()));
        }
    }

    public String getFunctionReturnType() {
        return names.getInnerType(function.getReturnType());
    }

    public Optional<String> getBoundTypeWithNamespace() {
        if (!function.isIsBound()) {
            return Optional.empty();
        } else {
            // TODO what does EntitySetPath mean?
            return Util.filter(function.getParameterOrAnnotation(), TActionFunctionParameter.class)
                    .map(x -> names.getInnerType(x)) //
                    .findFirst();
        }
    }

    public String getName() {
        return function.getName();
    }

    public String getActionMethodName() {
        return Names.getIdentifier(function.getName());
    }

    public List<Parameter> getParametersUnbound(Imports imports) {
        AtomicBoolean first = new AtomicBoolean(true);
        return Util.filter(function.getParameterOrAnnotation(), TActionFunctionParameter.class) //
                .filter(x -> !function.isIsBound() || !first.getAndSet(false)) //
                .map(x -> new Parameter(x, names, imports)) //
                .collect(Collectors.toList());
    }

    public static final class Parameter implements HasNameJavaHasNullable {
        public final String name;
        private final String nameJava;
        public final String importedFullClassName;

        public final boolean isCollection;
        public String typeWithNamespace;
        private final boolean isNullable;

        public Parameter(TActionFunctionParameter p, Names names, Imports imports) {
            this.name = p.getName();
            this.nameJava = Names.getIdentifier(p.getName());
            this.importedFullClassName = names.toImportedFullClassName(p, imports);
            this.isCollection = names.isCollection(p);
            this.typeWithNamespace = p.getType().get(0);
            this.isNullable = p.isNullable() == null? false: p.isNullable();
        }

        @Override
        public String nameJava() {
            return nameJava;
        }

        @Override
        public boolean isNullable() {
            return isNullable;
        }
    }

    public static final class ReturnType {
        public final String innerImportedFullClassName;
        public final boolean isCollection;

        public final String schemaInfoFullClassName;

        public ReturnType(boolean isCollection, String innerImportedFullClassName,
                String schemaInfoFullClassName) {
            this.isCollection = isCollection;
            this.innerImportedFullClassName = innerImportedFullClassName;
            this.schemaInfoFullClassName = schemaInfoFullClassName;
        }
    }

    public ReturnType getReturnType(Imports imports) {
        String innerType = names.getInnerType(function.getReturnType());
        final String schemaInfoClassName;
        if (innerType.startsWith("Edm.")) {
            schemaInfoClassName = EdmSchemaInfo.INSTANCE.getClass().getCanonicalName();
        } else {
            schemaInfoClassName = names.getFullClassNameSchemaInfo(names.getSchema(innerType));
        }

        return new ReturnType( //
                names.isCollection(function.getReturnType()), //
                names.toImportedTypeNonCollection(names.getInnerType(function.getReturnType()),
                        imports), //
                schemaInfoClassName);
    }

    public String getReturnTypeFullClassNameSchemaInfo() {
        return names.getFullClassNameSchemaInfo(names.getSchema(names.getInnerType(function.getReturnType())));
    }

    public String getFullType() {
        return names.getFullTypeFromSimpleType(schema(), function.getName());
    }

    public boolean isBoundToCollection() {
        return getBoundType().map(x -> Names.isCollection(x)).orElse(false);
    }

    public Optional<String> getBoundType() {
        if (!function.isIsBound()) {
            return Optional.empty();
        } else {
            TActionFunctionParameter p = Util
                    .filter(function.getParameterOrAnnotation(), TActionFunctionParameter.class)
                    .findFirst() //
                    .get();
            return Optional.of(p.getType().get(0));
        }
    }
}
