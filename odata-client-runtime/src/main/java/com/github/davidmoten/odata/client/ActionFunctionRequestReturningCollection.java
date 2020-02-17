package com.github.davidmoten.odata.client;

import java.util.Collection;
import java.util.Map;

public final class ActionFunctionRequestReturningCollection<T>
        extends ActionRequestBase<ActionFunctionRequestReturningCollection<T>> {

    public ActionFunctionRequestReturningCollection(ContextPath contextPath, Class<T> returnClass,
            Map<String, Object> parameters) {
        super(parameters, contextPath);
    }

    public Collection<T> get() {
        // TODO
        throw new UnsupportedOperationException();
    }

}