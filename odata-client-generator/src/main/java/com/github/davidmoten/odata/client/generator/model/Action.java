package com.github.davidmoten.odata.client.generator.model;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.oasisopen.odata.csdl.v4.Schema;
import org.oasisopen.odata.csdl.v4.TAction;
import org.oasisopen.odata.csdl.v4.TActionFunctionParameter;
import org.oasisopen.odata.csdl.v4.TActionFunctionReturnType;

import com.github.davidmoten.odata.client.generator.Imports;
import com.github.davidmoten.odata.client.generator.Names;
import com.github.davidmoten.odata.client.generator.Util;

public final class Action {

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
            return Util.filter(action.getParameterOrAnnotationOrReturnType(), TActionFunctionParameter.class)
                    .filter(x -> x.getName().equals(action.getEntitySetPath())) // s
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

    public List<Parameter> getParameters(Imports imports) {
        return Util.filter(action.getParameterOrAnnotationOrReturnType(), TActionFunctionParameter.class) //
                .filter(x -> !action.isIsBound() || !x.getName().equals(action.getEntitySetPath())) //
                .map(x -> new Parameter(x, names, imports)) //
                .collect(Collectors.toList());
    }

    public static final class Parameter {
        public final String name;
        public final String nameJava;
        public final String importedFullClassName;

        public final boolean isCollection;

        public Parameter(TActionFunctionParameter p, Names names, Imports imports) {
            this.name = p.getName();
            this.nameJava = Names.getIdentifier(p.getName());
            this.importedFullClassName = names.toImportedFullClassName(p, imports);
            this.isCollection = names.isCollection(p);
        }
    }

    public static final class ReturnType {
        public final String innerImportedFullClassName;
        public final boolean isCollection;

        public ReturnType(boolean isCollection, String innerImportedFullClassName) {
            this.isCollection = isCollection;
            this.innerImportedFullClassName = innerImportedFullClassName;
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
                .map(x -> new ReturnType( //
                        names.isCollection(x), //
                        names.toImportedTypeNonCollection(names.getInnerType(x), imports))) //
                .get();
    }
}
