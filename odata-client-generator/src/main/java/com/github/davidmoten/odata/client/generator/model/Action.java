package com.github.davidmoten.odata.client.generator.model;

import java.io.File;

import org.oasisopen.odata.csdl.v4.Schema;
import org.oasisopen.odata.csdl.v4.TAction;

import com.github.davidmoten.odata.client.generator.Names;

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

    public Class<?> getFullClassNameActionReturnType() {
        // TODO Auto-generated method stub
        return null;
    }

    public File getClassFileActionRequest() {
        return names.getClassFileEntity(schema(), action.getName());
    }

}
