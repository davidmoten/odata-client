package com.github.davidmoten.odata.client.generator.model;

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

    private final TAction action;
    private final Names names;

    public Action(TAction action, Names names) {
        this.action = action;
        this.names = names;
    }

    public Schema schema() {
        return names.getSchema(action);
    }

    public String getFullClassNameActionReturnType() {
        Optional<TActionFunctionReturnType> returnParameter = Util
                .filter(action.getParameterOrAnnotationOrReturnType(),
                        TActionFunctionReturnType.class)
                .findFirst();
        if (!returnParameter.isPresent()) {
            return Void.class.getName();
        } else {
            return names.getFullClassNameFromTypeWithNamespace(
                    names.getInnerType(returnParameter.get()));
        }
    }

    public Optional<String> getActionReturnType() {
        return Util
                .filter(action.getParameterOrAnnotationOrReturnType(),
                        TActionFunctionReturnType.class) //
                .map(names::getInnerType) //
                .findFirst();
    }

    public Optional<String> getBoundTypeWithNamespace() {
        if (!action.isIsBound()) {
            return Optional.empty();
        } else {
            // TODO what does EntitySetPath mean?
            return Util
                    .filter(action.getParameterOrAnnotationOrReturnType(),
                            TActionFunctionParameter.class)
                    .map(names::getInnerType) //
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
        return Util
                .filter(action.getParameterOrAnnotationOrReturnType(),
                        TActionFunctionParameter.class) //
                .filter(x -> !action.isIsBound() || !first.getAndSet(false)) //
                .map(x -> new Parameter(x, names, imports)) //
                .collect(Collectors.toList());
    }

    public static final class Parameter implements HasNameJavaHasNullable {
        public final String name;
        private final String nameJava;
        public final String importedFullClassName;

        public final boolean isCollection;
        public final String typeWithNamespace;

        private final boolean isNullable;
        private final boolean isAscii;

        public Parameter(TActionFunctionParameter p, Names names, Imports imports) {
            this.name = p.getName();
            this.nameJava = Names.getIdentifier(p.getName());
            this.importedFullClassName = names.toImportedFullClassName(p, imports);
            this.isCollection = names.isCollection(p);
            this.typeWithNamespace = p.getType().get(0);
            this.isNullable = p.isNullable() == null ? true : p.isNullable();
            this.isAscii = p.isUnicode()!= null && !p.isUnicode();
        }

        @Override
        public String nameJava() {
            return nameJava;
        }

        @Override
        public boolean isNullable() {
            return isNullable;
        }
        
        public boolean isAscii() {
            return isAscii; 
        }
    }

    public static final class ReturnType {
        public final String innerImportedFullClassName;
        public final boolean isCollection;
        public final String innerType;

        public ReturnType(String innerType, boolean isCollection, String innerImportedFullClassName) {
            this.innerType = innerType;
            this.isCollection = isCollection;
            this.innerImportedFullClassName = innerImportedFullClassName;
        }
    }

    public boolean hasReturnType() {
        return Util
                .filter(action.getParameterOrAnnotationOrReturnType(),
                        TActionFunctionReturnType.class) //
                .findFirst() //
                .isPresent();
    }

    public ReturnType getReturnType(Imports imports) {
        return Util
                .filter(action.getParameterOrAnnotationOrReturnType(),
                        TActionFunctionReturnType.class) //
                .findFirst() //
                .map(x -> {
                    String innerType = names.getInnerType(x);
                    return new ReturnType( //
                            innerType, //
                            names.isCollection(x), //
                            names.toImportedTypeNonCollection(names.getInnerType(x), imports));
                }) //
                .get();
    }

    public String getFullType() {
        return names.getFullTypeFromSimpleType(schema(), action.getName());
    }

    public boolean isBoundToCollection() {
        return getBoundType().map(Names::isCollection).orElse(false);
    }

    public Optional<String> getBoundType() {
        if (!action.isIsBound()) {
            return Optional.empty();
        } else {
            TActionFunctionParameter p = Util.filter(action.getParameterOrAnnotationOrReturnType(),
                    TActionFunctionParameter.class).findFirst().get();
            return Optional.of(p.getType().get(0));
        }
    }
}
