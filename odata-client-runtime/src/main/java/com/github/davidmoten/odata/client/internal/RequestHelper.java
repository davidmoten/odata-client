package com.github.davidmoten.odata.client.internal;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.github.davidmoten.odata.client.ClientException;
import com.github.davidmoten.odata.client.ContextPath;
import com.github.davidmoten.odata.client.HttpResponse;
import com.github.davidmoten.odata.client.ODataEntity;
import com.github.davidmoten.odata.client.RequestOptions;
import com.github.davidmoten.odata.client.SchemaInfo;
import com.github.davidmoten.odata.client.Serializer;
import com.github.davidmoten.odata.client.HttpService;

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

        String json = Serializer.INSTANCE.serialize(entity);

        Map<String, String> h = supplementRequestHeaders(options, "minimal");

        // get the response
        HttpResponse response = cp.context().service().POST(cp.toUrl(), h, json);

        // deserialize
        if (response.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
            throw new RuntimeException("Returned response code " + response.getResponseCode()
                    + " from url=" + cp.toUrl() + ", expected 204 (NO_CONTENT)");
        }

        // deserialize
        Class<? extends T> c = getSubClass(cp, schemaInfo, cls,
                response.getText(HttpURLConnection.HTTP_CREATED));
        // check if we need to deserialize into a subclass of T (e.g. return a
        // FileAttachment which is a subclass of Attachment)
        return cp.context().serializer()
                .deserialize(response.getText(HttpURLConnection.HTTP_CREATED), c, contextPath);
    }

    public static <T extends ODataEntity> T patch(T entity, ContextPath contextPath,
            RequestOptions options, SchemaInfo schemaInfo) {
        return patch(entity, contextPath, options, schemaInfo, false);
    }

    public static <T extends ODataEntity> void delete(ContextPath cp, RequestOptions options) {
        String url = cp.toUrl();
        HttpResponse response = cp.context().service().DELETE(url, options.getRequestHeaders());
        if (response.getResponseCode() != HttpURLConnection.HTTP_NO_CONTENT) {
            throw new ClientException("Returned response code " + response.getResponseCode()
                    + " from DELETE to url=" + url + ", expected 204 (NO_CONTENT)");
        }
    }

    public static <T extends ODataEntity> T put(T entity, ContextPath contextPath,
            RequestOptions options, SchemaInfo schemaInfo) {
        return patch(entity, contextPath, options, schemaInfo, true);
    }

    private static <T extends ODataEntity> T patch(T entity, ContextPath contextPath,
            RequestOptions options, SchemaInfo schemaInfo, boolean usePUT) {

        String json = Serializer.INSTANCE.serializeChangesOnly(entity);

        // build the url
        ContextPath cp = contextPath.addQueries(options.getQueries());

        Map<String, String> h = supplementRequestHeaders(options, "minimal");

        final String url;
        String editLink = (String) entity.getUnmappedFields().get("@odata.editLink");
        if (editLink != null) {
            if (editLink.startsWith("https://") || editLink.startsWith("http://")) {
                url = editLink;
            } else {
                // TOOD unit test relative url in editLink
                // from
                // http://docs.oasis-open.org/odata/odata-json-format/v4.01/cs01/odata-json-format-v4.01-cs01.html#_Toc499720582
                String context = (String) entity.getUnmappedFields().get("@odata.context");
                if (context != null) {
                    try {
                        URL u = new URL(context);
                        String p = u.getPath();
                        String basePath = p.substring(0, p.lastIndexOf('/'));
                        url = basePath + "/" + editLink;
                    } catch (MalformedURLException e) {
                        throw new ClientException(e);
                    }
                } else {
                    url = cp.context().service().getBasePath().toUrl() + "/" + editLink;
                }
            }
        } else {
            url = cp.toUrl();
        }
        // get the response
        HttpService service = cp.context().service();
        final HttpResponse response;
        if (usePUT) {
            response = service.PUT(url, h, json);
        } else {
            response = service.PATCH(url, h, json);
        }
        // deserialize
        if (response.getResponseCode() != HttpURLConnection.HTTP_NO_CONTENT) {
            throw new RuntimeException("Returned response code " + response.getResponseCode()
                    + " from PATCH/PUT at url=" + url + ", expected 204 (NO_CONTENT)");
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
