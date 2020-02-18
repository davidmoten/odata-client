package com.github.davidmoten.odata.client;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.github.davidmoten.odata.client.internal.RequestHelper;

public final class ActionRequestReturningCollection<T>
        extends ActionFunctionRequestBase<ActionRequestReturningCollection<T>> {

    private final SchemaInfo returnTypeSchemaInfo;

    public ActionRequestReturningCollection(ContextPath contextPath, Class<T> returnClass,
            Map<String, Object> parameters, SchemaInfo returnTypeSchemaInfo) {
        super(parameters, contextPath);
        this.returnTypeSchemaInfo = returnTypeSchemaInfo;
    }

    @SuppressWarnings("unchecked")
    public Collection<T> get() {
        return RequestHelper.postAny(parameters,contextPath, List.class, this, returnTypeSchemaInfo);
    }

}