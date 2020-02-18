package com.github.davidmoten.odata.client;

import java.util.Map;

import com.github.davidmoten.odata.client.internal.RequestHelper;

public final class ActionRequestNoReturn extends ActionFunctionRequestBase<ActionRequestNoReturn> {
    
    public ActionRequestNoReturn(ContextPath contextPath, Map<String, Object> parameters) {
        super(parameters, contextPath);
    }

    public void call() {
        RequestHelper.post(parameters, contextPath, this);
    }

}