package com.github.davidmoten.odata.client.generator.model;

import java.io.File;
import java.util.Optional;

import org.oasisopen.odata.csdl.v4.Schema;
import org.oasisopen.odata.csdl.v4.TAction;
import org.oasisopen.odata.csdl.v4.TActionFunctionParameter;
import org.oasisopen.odata.csdl.v4.TActionFunctionReturnType;
import org.oasisopen.odata.csdl.v4.TProperty;

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

}
