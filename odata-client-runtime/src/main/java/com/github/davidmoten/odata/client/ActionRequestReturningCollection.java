package com.github.davidmoten.odata.client;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public final class ActionRequestReturningCollection<T>
        extends ActionFunctionRequestBase<ActionRequestReturningCollection<T>> implements Iterable<T> {

    private final SchemaInfo returnTypeSchemaInfo;
    private final Class<T> returnClass;
    private final Map<String, Object> parameters;

    public ActionRequestReturningCollection(ContextPath contextPath, Class<T> returnClass,
            Map<String, Object> parameters, SchemaInfo returnTypeSchemaInfo) {
        super(parameters, contextPath);
        this.returnClass = returnClass;
        this.returnTypeSchemaInfo = returnTypeSchemaInfo;
        this.parameters = parameters;
    }

    public CollectionPageNonEntityRequest<T> get() {
        // TODO add request headers
        String json = Serializer.INSTANCE.serialize(parameters);
        return new CollectionPageNonEntityRequest<T>(contextPath.addQueries(queries), returnClass, returnTypeSchemaInfo, HttpMethod.POST,
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