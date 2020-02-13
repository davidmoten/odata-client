package com.github.davidmoten.odata.client;

import java.util.Collection;
import java.util.Map;

public final class ActionRequestReturningCollection<T>
        extends ActionRequestBase<ActionRequestReturningCollection<T>> {

    public ActionRequestReturningCollection(ContextPath contextPath, Class<T> returnClass,
            Map<String, Object> parameters) {
        super();
    }

    public Collection<T> get() {
        // TODO
        throw new UnsupportedOperationException();
    }

}