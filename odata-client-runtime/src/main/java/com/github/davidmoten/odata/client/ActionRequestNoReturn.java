package com.github.davidmoten.odata.client;

import java.util.Map;

public final class ActionRequestNoReturn extends ActionRequestBase<ActionRequestNoReturn> {

    public ActionRequestNoReturn(ContextPath contextPath, Map<String, Object> parameters) {
        super();
    }

    public void call() {
        // TODO
        throw new UnsupportedOperationException();
    }

}