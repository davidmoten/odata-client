package com.github.davidmoten.odata.client.internal;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.odata.client.ClientException;
import com.github.davidmoten.odata.client.Context;
import com.github.davidmoten.odata.client.ContextPath;
import com.github.davidmoten.odata.client.HasUnmappedFields;
import com.github.davidmoten.odata.client.HttpResponse;
import com.github.davidmoten.odata.client.HttpService;
import com.github.davidmoten.odata.client.ODataEntity;
import com.github.davidmoten.odata.client.Path;
import com.github.davidmoten.odata.client.RequestHeader;
import com.github.davidmoten.odata.client.RequestOptions;
import com.github.davidmoten.odata.client.SchemaInfo;
import com.github.davidmoten.odata.client.Serializer;
import com.github.davidmoten.odata.client.StreamProvider;

public final class RequestHelper {

    private static final String CONTENT_TYPE_APPLICATION_OCTET_STREAM = "application/octet-stream";

    private RequestHelper() {
        // prevent instantiation
    }

    public static <T extends ODataEntity> T get(ContextPath contextPath, Class<T> cls, RequestOptions options,
            SchemaInfo schemaInfo) {
        // build the url
        ContextPath cp = contextPath.addQueries(options.getQueries());

        List<RequestHeader> h = supplementRequestHeaders(options, "minimal");

        // get the response
        HttpResponse response = cp.context().service().get(cp.toUrl(), h);

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

        List<RequestHeader> h = supplementRequestHeaders(options, "minimal");

        // get the response
        HttpResponse response = cp.context().service().post(cp.toUrl(), h, json);

        // deserialize
        if (response.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
            throw new RuntimeException("Returned response code " + response.getResponseCode() + " from url="
                    + cp.toUrl() + ", expected 204 (NO_CONTENT)");
        }

        // deserialize
        Class<? extends T> c = getSubClass(cp, schemaInfo, cls, response.getText(HttpURLConnection.HTTP_CREATED));
        // check if we need to deserialize into a subclass of T (e.g. return a
        // FileAttachment which is a subclass of Attachment)
        return cp.context().serializer().deserialize(response.getText(HttpURLConnection.HTTP_CREATED), c, contextPath);
    }

    public static <T extends ODataEntity> T patch(T entity, ContextPath contextPath, RequestOptions options,
            SchemaInfo schemaInfo) {
        return patch(entity, contextPath, options, schemaInfo, false);
    }

    public static <T extends ODataEntity> void delete(ContextPath cp, RequestOptions options) {
        String url = cp.toUrl();
        HttpResponse response = cp.context().service().delete(url, options.getRequestHeaders());
        if (response.getResponseCode() != HttpURLConnection.HTTP_NO_CONTENT) {
            throw new ClientException("Returned response code " + response.getResponseCode() + " from DELETE to url="
                    + url + ", expected 204 (NO_CONTENT)");
        }
    }

    public static <T extends ODataEntity> T put(T entity, ContextPath contextPath, RequestOptions options,
            SchemaInfo schemaInfo) {
        return patch(entity, contextPath, options, schemaInfo, true);
    }

    private static <T extends ODataEntity> T patch(T entity, ContextPath contextPath, RequestOptions options,
            SchemaInfo schemaInfo, boolean usePUT) {

        String json = Serializer.INSTANCE.serializeChangesOnly(entity);

        // build the url
        ContextPath cp = contextPath.addQueries(options.getQueries());

        List<RequestHeader> h = supplementRequestHeaders(options, "minimal");

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
            response = service.put(url, h, json);
        } else {
            response = service.patch(url, h, json);
        }
        // deserialize
        if (response.getResponseCode() < 200 || response.getResponseCode() >= 300) {
            throw new RuntimeException("Returned response code " + response.getResponseCode()
                    + " from PATCH/PUT at url=" + url + ", expected 204 (NO_CONTENT)");
        }
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

    private static List<RequestHeader> supplementRequestHeaders(RequestOptions options, String odataMetadataValue) {
        List<RequestHeader> h = new ArrayList<>();
        h.add(new RequestHeader("OData-Version", "4.0"));
        h.add(new RequestHeader("Content-Type", "application/json;odata.metadata=" + odataMetadataValue));
        h.add(new RequestHeader("Accept", "application/json"));
        h.addAll(options.getRequestHeaders());
        return h;
    }

    public static InputStream getStream(ContextPath contextPath, RequestOptions options, String base64) {
        if (base64 != null) {
            return new ByteArrayInputStream(Base64.getDecoder().decode(base64));
        } else {
            ContextPath cp = contextPath.addQueries(options.getQueries());
            return contextPath.context().service().getStream(cp.toUrl(), options.getRequestHeaders());
        }
    }

    // for HasStream case (only for entities, not for complexTypes)
    public static Optional<StreamProvider> createStream(ContextPath contextPath, ODataEntity entity) {
        String editLink = (String) entity.getUnmappedFields().get("@odata.editLink");
        String contentType = (String) entity.getUnmappedFields().get("@odata.mediaContentType");
        if (editLink == null) {
            return Optional.empty();
        } else {
            if (contentType == null) {
                contentType = CONTENT_TYPE_APPLICATION_OCTET_STREAM;
            }
            // TODO support relative editLink?
            Context context = contextPath.context();
            Path path = new Path(editLink, contextPath.path().style()).addSegment("$value");
            return Optional.of(new StreamProvider( //
                    new ContextPath(context, path), //
                    RequestOptions.EMPTY, //
                    contentType, //
                    null));
        }
    }

    public static Optional<StreamProvider> createStreamForEdmStream(ContextPath contextPath, HasUnmappedFields item,
            String fieldName, String base64) {
        Preconditions.checkNotNull(fieldName);
        String readLink = (String) item.getUnmappedFields().get(fieldName + "@odata.mediaReadLink");
        String contentType = (String) item.getUnmappedFields().get(fieldName + "@odata.mediaContentType");
        if (readLink == null && base64 != null) {
            return Optional.empty();
        } else {
            if (contentType == null) {
                contentType = CONTENT_TYPE_APPLICATION_OCTET_STREAM;
            }
            // TODO support relative editLink?
            Context context = contextPath.context();
            Path path = new Path(readLink, contextPath.path().style()).addSegment("$value");
            return Optional.of(new StreamProvider( //
                    new ContextPath(context, path), //
                    RequestOptions.EMPTY, //
                    contentType, //
                    base64));
        }
    }

}
