package com.github.davidmoten.odata.client.internal;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.github.davidmoten.odata.client.ContextPath;
import com.github.davidmoten.odata.client.HttpResponse;
import com.github.davidmoten.odata.client.ODataEntity;
import com.github.davidmoten.odata.client.RequestOptions;
import com.github.davidmoten.odata.client.SchemaInfo;
import com.github.davidmoten.odata.client.Serializer;

public final class RequestHelper {

    private RequestHelper() {
        // prevent instantiation
    }

    public static <T extends ODataEntity> T get(ContextPath contextPath, Class<T> cls,
            RequestOptions options, SchemaInfo schemaInfo) {
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

        String json = Serializer.DEFAULT.serialize(entity);

        // build the url
        ContextPath cp = contextPath.addQueries(options.getQueries());
        
        Map<String, String> h = new HashMap<>();
        h.put("OData-Version", "4.0");
        h.put("Content-Type", "application/json;odata.metadata=minimal");
        h.put("Accept", "application/json");
        h.putAll(options.getRequestHeaders());

        // get the response
        HttpResponse response = cp.context().service().PATCH(cp.toUrl(), h, json);
        // deserialize
        if (response.getResponseCode() != HttpURLConnection.HTTP_NO_CONTENT) {
            throw new RuntimeException("Returned response code " + response.getResponseCode()
                    + " from url=" + cp.toUrl() + ", expected 204 (NO_CONTENT)");
        }
        return entity;
    }

    @SuppressWarnings("unchecked")
    public static <T extends ODataEntity> Class<? extends T> getSubClass(ContextPath cp,
            SchemaInfo schemaInfo, Class<T> cls, String json) {
        Optional<String> namespacedType = cp.context().serializer().getODataType(json)
                .map(x -> x.substring(1));

        if (namespacedType.isPresent()) {
            return (Class<? extends T>) schemaInfo
                    .getEntityClassFromTypeWithNamespace(namespacedType.get());
        } else {
            return cls;
        }
    }

}
