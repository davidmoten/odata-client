package com.github.davidmoten.odata.client;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.github.davidmoten.odata.client.internal.RequestHelper;

public final class FunctionRequestReturningCollection<T>
        extends ActionFunctionRequestBase<FunctionRequestReturningCollection<T>> {

    private final SchemaInfo schemaInfo;

    public FunctionRequestReturningCollection(ContextPath contextPath, Class<T> returnClass,
            Map<String, Object> parameters, SchemaInfo schemaInfo) {
        super(parameters, contextPath);
        this.schemaInfo = schemaInfo;
    }

    @SuppressWarnings("unchecked")
    public Collection<T> get() {
        return RequestHelper.get(contextPath, List.class, this, schemaInfo);
    }

}