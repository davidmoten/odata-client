package com.github.davidmoten.odata.client;

import java.util.Map;

public final class ActionRequestReturningNonCollection<T>
        extends ActionRequestBase<ActionRequestReturningNonCollection<T>> {

    public ActionRequestReturningNonCollection(ContextPath contextPath, Class<T> returnClass,
            Map<String, Object> parameters) {
        super(parameters, contextPath);
    }

    public T get() {
        // TODO
        throw new UnsupportedOperationException();
    }

}