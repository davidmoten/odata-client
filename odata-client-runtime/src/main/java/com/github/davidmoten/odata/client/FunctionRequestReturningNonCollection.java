package com.github.davidmoten.odata.client;

import java.util.Map;

import com.github.davidmoten.odata.client.internal.RequestHelper;
import com.github.davidmoten.odata.client.internal.TypedObject;

public final class FunctionRequestReturningNonCollection<T>
        extends ActionFunctionRequestBase<FunctionRequestReturningNonCollection<T>> {

    private final Class<T> returnClass;
    private final SchemaInfo schemaInfo;

    public FunctionRequestReturningNonCollection(ContextPath contextPath, Class<T> returnClass,
            Map<String, TypedObject> parameters, SchemaInfo schemaInfo) {
        super(parameters, contextPath);
        this.returnClass = returnClass;
        this.schemaInfo = schemaInfo;
    }

    @SuppressWarnings("unchecked")
    public ODataValue<T> get() {
        Serializer serializer = contextPath.context().serializer();
        return RequestHelper.getWithParametricType( //
                contextPath.addSegment(InlineParameterSyntax.encode(serializer, parameters)), //
                ODataValue.class, //
                returnClass, //
                this, //
                schemaInfo);
    }

}