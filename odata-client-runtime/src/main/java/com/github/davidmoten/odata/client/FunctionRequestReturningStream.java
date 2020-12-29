package com.github.davidmoten.odata.client;

import java.io.InputStream;
import java.util.Map;

import com.github.davidmoten.odata.client.internal.RequestHelper;
import com.github.davidmoten.odata.client.internal.TypedObject;

public final class FunctionRequestReturningStream extends
        ActionFunctionRequestBase<FunctionRequestReturningStream> implements StreamProviderBase {

    public FunctionRequestReturningStream(ContextPath contextPath, Map<String, TypedObject> parameters) {
        super(parameters, contextPath);
    }

    @Override
    public InputStream get() {
        Serializer serializer = contextPath.context().serializer();
        return RequestHelper.getStream(
                contextPath.appendToSegment(InlineParameterSyntax.encode(serializer, parameters)),
                options(), null);
    }

}
