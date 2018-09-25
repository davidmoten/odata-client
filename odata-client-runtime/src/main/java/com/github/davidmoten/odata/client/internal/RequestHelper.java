package com.github.davidmoten.odata.client.internal;

import java.util.Optional;

import com.github.davidmoten.odata.client.ContextPath;
import com.github.davidmoten.odata.client.ODataEntity;
import com.github.davidmoten.odata.client.RequestOptions;
import com.github.davidmoten.odata.client.HttpResponse;
import com.github.davidmoten.odata.client.SchemaInfo;

public final class RequestHelper {

    private RequestHelper() {
        // prevent instantiation
    }

    public static <T extends ODataEntity> T get(ContextPath contextPath, Class<T> cls, RequestOptions options,
            SchemaInfo schemaInfo) {
        // build the url
        ContextPath cp = contextPath.addQueries(options.getQueries());
        // get the response
        HttpResponse response = cp.context().service().GET(cp.toUrl(), options.getRequestHeaders());
        // deserialize

        Class<? extends T> c = getSubClass(cp, schemaInfo, cls, response.getText());
        // check if we need to deserialize into a subclass of T (e.g. return a
        // FileAttachment which is a subclass of Attachment)
        return cp.context().serializer().deserialize(response.getText(), c, contextPath);
    }

    public static <T extends ODataEntity> T patch(T entity, ContextPath contextPath, Class<T> cls,
            RequestOptions options, SchemaInfo schemaInfo) {
        // build the url
        ContextPath cp = contextPath.addQueries(options.getQueries());
        // get the response
        HttpResponse response = cp.context().service().PATCH(cp.toUrl(), options.getRequestHeaders());
        // deserialize

        return entity;
    }

    @SuppressWarnings("unchecked")
    public static <T extends ODataEntity> Class<? extends T> getSubClass(ContextPath cp, SchemaInfo schemaInfo,
            Class<T> cls, String json) {
        Optional<String> namespacedType = cp.context().serializer().getODataType(json).map(x -> x.substring(1));

        if (namespacedType.isPresent()) {
            return (Class<? extends T>) schemaInfo.getEntityClassFromTypeWithNamespace(namespacedType.get());
        } else {
            return cls;
        }
    }

}
