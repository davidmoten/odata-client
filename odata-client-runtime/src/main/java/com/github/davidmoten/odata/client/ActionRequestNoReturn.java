package com.github.davidmoten.odata.client;

import java.util.Map;

import com.github.davidmoten.odata.client.internal.RequestHelper;

public final class ActionRequestNoReturn extends ActionRequestBase<ActionRequestNoReturn> {

    private final Map<String, Object> parameters;
    private final ContextPath contextPath;

    public ActionRequestNoReturn(ContextPath contextPath, Map<String, Object> parameters) {
        this.parameters = parameters;
        this.contextPath = contextPath;
    }

    public void call() {
        RequestOptions requestOptions = RequestOptions.EMPTY;
        SchemaInfo schemaInfo = null;
        RequestHelper.post(parameters, contextPath, requestOptions, schemaInfo);
    }

}