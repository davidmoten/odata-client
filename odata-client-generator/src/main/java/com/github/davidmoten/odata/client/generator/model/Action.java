package com.github.davidmoten.odata.client.generator.model;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.oasisopen.odata.csdl.v4.Schema;
import org.oasisopen.odata.csdl.v4.TAction;
import org.oasisopen.odata.csdl.v4.TActionFunctionParameter;
import org.oasisopen.odata.csdl.v4.TActionFunctionReturnType;

import com.github.davidmoten.odata.client.generator.Imports;
import com.github.davidmoten.odata.client.generator.Names;
import com.github.davidmoten.odata.client.generator.Util;
import com.github.davidmoten.odata.client.internal.EdmSchemaInfo;

public final class Action implements Method {

    private TAction action;
    private Names names;

    public Action(TAction action, Names names) {
        this.action = action;
        this.names = names;
    }

    public String getSimpleClassNameActionRequest() {
        return names.getSimpleClassNameActionRequest(schema(), action.getName());
    }

    public Schema schema() {
        return names.getSchema(action);
    }

    public String getFullClassNameActionRequest() {
        return names.getFullClassNameActionRequest(schema(), action.getName());
    }

    public Object getPackageActionRequest() {
        return names.getPackageActionRequest(schema());
    }

    public String getFullClassNameActionReturnType() {
        Optional<TActionFunctionReturnType> returnParameter = Util
                .filter(action.getParameterOrAnnotationOrReturnType(), TActionFunctionReturnType.class).findFirst();
        if (!returnParameter.isPresent()) {
            return Void.class.getName();
        } else {
            return names.getFullClassNameFromTypeWithNamespace(names.getInnerType(returnParameter.get()));
        }
    }

    public File getClassFileActionRequest() {
        return names.getClassFileActionRequest(schema(), action.getName());
    }

    public Optional<String> getActionReturnType() {
        return Util.filter(action.getParameterOrAnnotationOrReturnType(), TActionFunctionReturnType.class) //
                .map(x -> names.getInnerType(x)) //
                .findFirst();
    }

    public Optional<String> getBoundTypeWithNamespace() {
        if (!action.isIsBound()) {
            return Optional.empty();
        } else {
            // TODO what does EntitySetPath mean?
            return Util.filter(action.getParameterOrAnnotationOrReturnType(), TActionFunctionParameter.class)
                    .map(x -> names.getInnerType(x)) //
                    .findFirst();
        }
    }

    public String getName() {
        return action.getName();
    }

    public String getActionMethodName() {
        return Names.getIdentifier(action.getName());
    }

    public List<Parameter> getParametersUnbound(Imports imports) {
        AtomicBoolean first = new AtomicBoolean(true);
        return Util.filter(action.getParameterOrAnnotationOrReturnType(), TActionFunctionParameter.class) //
                .filter(x -> !action.isIsBound() || !first.getAndSet(false)) //
                .map(x -> new Parameter(x, names, imports)) //
                .collect(Collectors.toList());
    }

    public static final class Parameter implements HasNameJavaHasNullable{
        public final String name;
        private final String nameJava;
        public final String importedFullClassName;

        public final boolean isCollection;
        public final String typeWithNamespace;

        private final boolean isNullable;

        public Parameter(TActionFunctionParameter p, Names names, Imports imports) {
            this.name = p.getName();
            this.nameJava = Names.getIdentifier(p.getName());
            this.importedFullClassName = names.toImportedFullClassName(p, imports);
            this.isCollection = names.isCollection(p);
            this.typeWithNamespace = p.getType().get(0);
            this.isNullable = p.isNullable() == null ? true : p.isNullable();
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

        public ReturnType(boolean isCollection, String innerImportedFullClassName, String schemaInfoFullClassName) {
            this.isCollection = isCollection;
            this.innerImportedFullClassName = innerImportedFullClassName;
            this.schemaInfoFullClassName = schemaInfoFullClassName;
        }
    }

    public boolean hasReturnType() {
        return Util.filter(action.getParameterOrAnnotationOrReturnType(), TActionFunctionReturnType.class) //
                .findFirst() //
                .isPresent();
    }

    public ReturnType getReturnType(Imports imports) {
        return Util.filter(action.getParameterOrAnnotationOrReturnType(), TActionFunctionReturnType.class) //
                .findFirst() //
                .map(x -> {
                    String innerType = names.getInnerType(x);
                    final String schemaInfoClassName;
                    if (innerType.startsWith("Edm.")) {
                        schemaInfoClassName = EdmSchemaInfo.INSTANCE.getClass().getCanonicalName();
                    } else {
                        schemaInfoClassName = names.getFullClassNameSchemaInfo(names.getSchema(innerType));
                    }

                    return new ReturnType( //
                            names.isCollection(x), //
                            names.toImportedTypeNonCollection(names.getInnerType(x), imports), //
                            schemaInfoClassName);
                }) //
                .get();
    }

    public String getFullType() {
        return names.getFullTypeFromSimpleType(schema(), action.getName());
    }

    public boolean isBoundToCollection() {
        return getBoundType().map(x -> Names.isCollection(x)).orElse(false);
    }

    public Optional<String> getBoundType() {
        if (!action.isIsBound()) {
            return Optional.empty();
        } else {
            TActionFunctionParameter p = Util
                    .filter(action.getParameterOrAnnotationOrReturnType(), TActionFunctionParameter.class).findFirst()
                    .get();
            return Optional.of(p.getType().get(0));
        }
    }
}
