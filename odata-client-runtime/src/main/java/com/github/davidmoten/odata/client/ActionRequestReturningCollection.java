package com.github.davidmoten.odata.client;

import java.util.Collection;

public final class ActionRequestReturningCollection<T>
        extends ActionRequestBase<ActionRequestReturningCollection<T>> {

    public ActionRequestReturningCollection(ContextPath contextPath, Class<T> returnClass,
            Object... parameters) {
        super();
    }

    public Collection<T> get() {
        // TODO
        throw new UnsupportedOperationException();
    }

}