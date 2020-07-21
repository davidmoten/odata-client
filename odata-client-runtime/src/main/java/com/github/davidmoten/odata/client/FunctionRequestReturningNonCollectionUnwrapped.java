package com.github.davidmoten.odata.client;

import java.util.Map;

import com.github.davidmoten.odata.client.internal.RequestHelper;
import com.github.davidmoten.odata.client.internal.TypedObject;

public final class FunctionRequestReturningNonCollectionUnwrapped<T>
        extends ActionFunctionRequestBase<FunctionRequestReturningNonCollectionUnwrapped<T>> {

    private final Class<T> returnClass;
    private final SchemaInfo schemaInfo;

    public FunctionRequestReturningNonCollectionUnwrapped(ContextPath contextPath, Class<T> returnClass,
            Map<String, TypedObject> parameters, SchemaInfo schemaInfo) {
        super(parameters, contextPath);
        this.returnClass = returnClass;
        this.schemaInfo = schemaInfo;
    }

    public T get() {
        Serializer serializer = contextPath.context().serializer();
        return RequestHelper.get( //
                contextPath.addSegment(InlineParameterSyntax.encode(serializer, parameters)), //
                returnClass, //
                options(), //
                schemaInfo);
    }

}