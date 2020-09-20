package com.github.davidmoten.odata.client;

import java.util.Map;

import com.github.davidmoten.odata.client.internal.ParameterMap;
import com.github.davidmoten.odata.client.internal.RequestHelper;
import com.github.davidmoten.odata.client.internal.TypedObject;

public final class ActionRequestReturningNonCollectionUnwrapped<T>
        extends ActionFunctionRequestBase<ActionRequestReturningNonCollectionUnwrapped<T>> {

    private final Class<T> returnClass;

    public ActionRequestReturningNonCollectionUnwrapped(ContextPath contextPath, Class<T> returnClass,
            Map<String, TypedObject> parameters) {
        super(parameters, contextPath);
        this.returnClass = returnClass;
    }

    public T get() {
        return RequestHelper.postAny( //
                ParameterMap.toMap(parameters), //
                contextPath, //
                returnClass, //
                options()); //
    }

}