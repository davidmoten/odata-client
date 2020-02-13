package com.github.davidmoten.odata.client;

public final class ActionRequestReturningNonCollection<T> extends ActionRequestBase<ActionRequestReturningNonCollection<T>> {
    
    public ActionRequestReturningNonCollection(ContextPath contextPath, Class<T> returnClass, Object... parameters) {
        super();
    }
    
    public T get() {
        //TODO 
        throw new UnsupportedOperationException();
    }
    
}