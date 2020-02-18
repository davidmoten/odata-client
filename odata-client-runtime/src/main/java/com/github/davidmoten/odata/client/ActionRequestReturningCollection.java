package com.github.davidmoten.odata.client;

import java.util.Collection;
import java.util.Map;

public final class ActionRequestReturningCollection<T>
        extends ActionFunctionRequestBase<ActionRequestReturningCollection<T>> {

    public ActionRequestReturningCollection(ContextPath contextPath, Class<T> returnClass,
            Map<String, Object> parameters, SchemaInfo returnTypeSchemaInfo) {
        super(parameters, contextPath);
    }

    public Collection<T> get() {
        throw new UnsupportedOperationException();
    }

}