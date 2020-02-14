package com.github.davidmoten.odata.client;

import java.util.Map;

import com.github.davidmoten.odata.client.internal.RequestHelper;

public final class ActionRequestReturningNonCollection<T>
        extends ActionRequestBase<ActionRequestReturningNonCollection<T>> {

    private final Class<T> returnClass;
    private final SchemaInfo schemaInfo;

    public ActionRequestReturningNonCollection(ContextPath contextPath, Class<T> returnClass,
            Map<String, Object> parameters, SchemaInfo schemaInfo) {
        super(parameters, contextPath);
        this.returnClass = returnClass;
        this.schemaInfo = schemaInfo;
    }

    public T get() {
        return RequestHelper.postAny(parameters,contextPath, returnClass, this, schemaInfo);
    }

}