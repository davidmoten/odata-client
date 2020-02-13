package com.github.davidmoten.odata.client;

public final class ActionRequestNoReturn extends ActionRequestBase<ActionRequestNoReturn> {

    public ActionRequestNoReturn(ContextPath contextPath, Object... parameters) {
        super();
    }

    public void call() {
        // TODO
        throw new UnsupportedOperationException();
    }

}