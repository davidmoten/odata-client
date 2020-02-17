package com.github.davidmoten.odata.client;

import java.util.Map;

import com.github.davidmoten.odata.client.internal.RequestHelper;

public final class ActionFunctionRequestReturningNonCollection<T>
        extends ActionFunctionRequestBase<ActionFunctionRequestReturningNonCollection<T>> {

    private final Class<T> returnClass;
    private final SchemaInfo schemaInfo;

    public ActionFunctionRequestReturningNonCollection(ContextPath contextPath, Class<T> returnClass,
            Map<String, Object> parameters, SchemaInfo schemaInfo) {
        super(parameters, contextPath);
        this.returnClass = returnClass;
        this.schemaInfo = schemaInfo;
    }

    public T get() {
        return RequestHelper.postAny(parameters,contextPath, returnClass, this, schemaInfo);
    }

}