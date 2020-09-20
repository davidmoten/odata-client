package com.github.davidmoten.odata.client;

import java.util.Map;

import com.github.davidmoten.odata.client.internal.ParameterMap;
import com.github.davidmoten.odata.client.internal.RequestHelper;
import com.github.davidmoten.odata.client.internal.TypedObject;

public final class ActionRequestReturningNonCollection<T>
        extends ActionFunctionRequestBase<ActionRequestReturningNonCollection<T>> {

    private final Class<T> returnClass;

    public ActionRequestReturningNonCollection(ContextPath contextPath, Class<T> returnClass,
            Map<String, TypedObject> parameters) {
        super(parameters, contextPath);
        this.returnClass = returnClass;
    }

    @SuppressWarnings("unchecked")
    public ODataValue<T> get() {
        return RequestHelper.postAnyWithParametricType( //
                ParameterMap.toMap(parameters), //
                contextPath, //
                ODataValue.class, //
                returnClass, //
                options());
    }

}