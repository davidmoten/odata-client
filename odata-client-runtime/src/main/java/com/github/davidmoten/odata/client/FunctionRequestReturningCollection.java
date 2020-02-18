package com.github.davidmoten.odata.client;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class FunctionRequestReturningCollection<T>
        extends ActionFunctionRequestBase<FunctionRequestReturningCollection<T>> implements Iterable<T> {

    private final SchemaInfo returnTypeSchemaInfo;
    private final Class<T> returnClass;
    private final Map<String, Object> parameters;

    public FunctionRequestReturningCollection(ContextPath contextPath, Class<T> returnClass,
            Map<String, Object> parameters, SchemaInfo returnTypeSchemaInfo) {
        super(parameters, contextPath);
        this.returnClass = returnClass;
        this.parameters = parameters;
        this.returnTypeSchemaInfo = returnTypeSchemaInfo;
    }

    public CollectionPageNonEntityRequest<T> get() {
        String json = Serializer.INSTANCE.serialize(parameters);
        return new CollectionPageNonEntityRequest<T>(contextPath, returnClass, returnTypeSchemaInfo, HttpMethod.POST,
                Optional.of(json));
    }

    @Override
    public Iterator<T> iterator() {
        return get().get().iterator();
    }

    public Stream<T> stream() {
        return get().get().stream();
    }

}