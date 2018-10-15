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
import com.github.davidmoten.odata.client.Service;

public final class RequestHelper {

    private RequestHelper() {
        // prevent instantiation
    }

    public static <T extends ODataEntity> T get(ContextPath contextPath, Class<T> cls,
            RequestOptions options, SchemaInfo schemaInfo) {
        // build the url
        ContextPath cp = contextPath.addQueries(options.getQueries());

        Map<String, String> h = supplementRequestHeaders(options, "minimal");

        // get the response
        HttpResponse response = cp.context().service().GET(cp.toUrl(), h);

        // deserialize
        Class<? extends T> c = getSubClass(cp, schemaInfo, cls, response.getText());
        // check if we need to deserialize into a subclass of T (e.g. return a
        // FileAttachment which is a subclass of Attachment)
        return cp.context().serializer().deserialize(response.getText(), c, contextPath);
    }

    public static <T extends ODataEntity> T post(T entity, ContextPath contextPath, Class<T> cls,
            RequestOptions options, SchemaInfo schemaInfo) {
        // build the url
        ContextPath cp = contextPath.addQueries(options.getQueries());

        String json = Serializer.DEFAULT.serialize(entity);

        Map<String, String> h = supplementRequestHeaders(options, "minimal");

        // get the response
        HttpResponse response = cp.context().service().POST(cp.toUrl(), h, json);

        // deserialize
        if (response.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
            throw new RuntimeException("Returned response code " + response.getResponseCode()
                    + " from url=" + cp.toUrl() + ", expected 204 (NO_CONTENT)");
        }

        // deserialize
        Class<? extends T> c = getSubClass(cp, schemaInfo, cls, response.getText());
        // check if we need to deserialize into a subclass of T (e.g. return a
        // FileAttachment which is a subclass of Attachment)
        return cp.context().serializer().deserialize(response.getText(), c, contextPath);
    }

    public static <T extends ODataEntity> T patch(T entity, ContextPath contextPath,
            RequestOptions options, SchemaInfo schemaInfo) {
        return patch(entity, contextPath, options, schemaInfo, false);

    }

    public static <T extends ODataEntity> T put(T entity, ContextPath contextPath,
            RequestOptions options, SchemaInfo schemaInfo) {
        return patch(entity, contextPath, options, schemaInfo, true);
    }

    private static <T extends ODataEntity> T patch(T entity, ContextPath contextPath,
            RequestOptions options, SchemaInfo schemaInfo, boolean usePUT) {

        String json = Serializer.DEFAULT.serializeChangesOnly(entity);

        // build the url
        ContextPath cp = contextPath.addQueries(options.getQueries());

        Map<String, String> h = supplementRequestHeaders(options, "minimal");

        final String url;
        String editLink = (String) entity.getUnmappedFields().get("@odata.editLink");
        if (editLink != null) {
            url = cp.context().service().getBasePath().toUrl() + "/" + editLink;
        } else {
            url = cp.toUrl();
        }
        // get the response
        Service service = cp.context().service();
        final HttpResponse response;
        if (usePUT) {
            response = service.PUT(url, h, json);
        } else {
            response = service.PATCH(url, h, json);
        }
        // deserialize
        if (response.getResponseCode() != HttpURLConnection.HTTP_NO_CONTENT) {
            throw new RuntimeException("Returned response code " + response.getResponseCode()
                    + " from url=" + url + ", expected 204 (NO_CONTENT)");
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

    private static Map<String, String> supplementRequestHeaders(RequestOptions options,
            String odataMetadataValue) {
        Map<String, String> h = new HashMap<>();
        h.put("OData-Version", "4.0");
        h.put("Content-Type", "application/json;odata.metadata=" + odataMetadataValue);
        h.put("Accept", "application/json");
        h.putAll(options.getRequestHeaders());
        return h;
    }

}
