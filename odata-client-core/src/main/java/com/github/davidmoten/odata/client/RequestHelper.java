package com.github.davidmoten.odata.client;

import java.util.Optional;

public final class RequestHelper {

    private RequestHelper() {
        // prevent instantiation
    }

    @SuppressWarnings("unchecked")
    public static <T extends ODataEntity> T get(ContextPath contextPath, Class<T> cls, RequestOptions options,
            SchemaInfo schemaInfo) {
        // build the url
        ContextPath cp = contextPath.addQueries(options.getQueries());
        // get the response
        ResponseGet response = cp.context().service().GET(cp.toUrl(), options.getRequestHeaders());
        // deserialize

        // check if we need to deserialize into a subclass of T (e.g. return a
        // FileAttachment which is a subclass of Attachment)
        Optional<String> namespacedType = cp.context().serializer().getODataType(response.getText());
        final Class<? extends T> c;
        if (namespacedType.isPresent()) {
            c = (Class<? extends T>) schemaInfo.getEntityClassFromTypeWithNamespace(namespacedType.get());
        } else {
            c = cls;
        }
        return cp.context().serializer().deserialize(response.getText(), c, contextPath);
    }

    public static <T extends ODataEntity> CollectionPageEntity<T> getCollection(ContextPath contextPath, Class<T> cls,
            RequestOptions options) {
        // build the url
        ContextPath cp = contextPath.addQueries(options.getQueries());
        // get the response
        ResponseGet response = cp.context().service().GET(cp.toUrl(), options.getRequestHeaders());
        // deserialize
        // return cp.context().serializer().deserialize(response.getText(), cls,
        // contextPath);
        return null;
    }

}
