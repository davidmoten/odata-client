package com.github.davidmoten.odata.client;

import java.util.Map;

import com.github.davidmoten.odata.client.internal.RequestHelper;
import com.github.davidmoten.odata.client.internal.TypedObject;

public final class FunctionRequestReturningNonCollection<T>
        extends ActionFunctionRequestBase<FunctionRequestReturningNonCollection<T>> {

    private final Class<T> returnClass;

    public FunctionRequestReturningNonCollection(ContextPath contextPath, Class<T> returnClass,
            Map<String, TypedObject> parameters) {
        super(parameters, contextPath);
        this.returnClass = returnClass;
    }

    @SuppressWarnings("unchecked")
    public ODataValue<T> get() {
        Serializer serializer = contextPath.context().serializer();
        return RequestHelper.getWithParametricType( //
                contextPath.addSegment(InlineParameterSyntax.encode(serializer, parameters)), //
                ODataValue.class, //
                returnClass, //
                options());
    }

}