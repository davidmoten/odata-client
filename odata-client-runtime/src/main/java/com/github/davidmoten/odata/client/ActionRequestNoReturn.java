package com.github.davidmoten.odata.client;

import java.util.Map;

import com.github.davidmoten.odata.client.internal.ParameterMap;
import com.github.davidmoten.odata.client.internal.RequestHelper;
import com.github.davidmoten.odata.client.internal.TypedObject;

public final class ActionRequestNoReturn extends ActionFunctionRequestBase<ActionRequestNoReturn> {

    public ActionRequestNoReturn(ContextPath contextPath, Map<String, TypedObject> parameters) {
        super(parameters, contextPath);
    }

    public void call() {
        RequestHelper.post(ParameterMap.toMap(parameters), contextPath, options());
    }

}