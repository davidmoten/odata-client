package com.github.davidmoten.odata.client;

import java.util.Map;

import com.github.davidmoten.odata.client.internal.ParameterMap;
import com.github.davidmoten.odata.client.internal.RequestHelper;
import com.github.davidmoten.odata.client.internal.TypedObject;

public final class ActionRequestReturningNonCollectionUnwrapped<T>
        extends ActionFunctionRequestBase<ActionRequestReturningNonCollectionUnwrapped<T>> {

    private final Class<T> returnClass;
    private final SchemaInfo schemaInfo;

    public ActionRequestReturningNonCollectionUnwrapped(ContextPath contextPath, Class<T> returnClass,
            Map<String, TypedObject> parameters, SchemaInfo schemaInfo) {
        super(parameters, contextPath);
        this.returnClass = returnClass;
        this.schemaInfo = schemaInfo;
    }

    public T get() {
        return RequestHelper.postAny( //
                ParameterMap.toMap(parameters), //
                contextPath, //
                returnClass, //
                options(), //
                schemaInfo); //
    }

}