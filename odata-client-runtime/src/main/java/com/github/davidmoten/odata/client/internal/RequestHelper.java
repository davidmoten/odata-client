package com.github.davidmoten.odata.client.internal;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.odata.client.ClientException;
import com.github.davidmoten.odata.client.Context;
import com.github.davidmoten.odata.client.ContextPath;
import com.github.davidmoten.odata.client.HttpMethod;
import com.github.davidmoten.odata.client.HttpResponse;
import com.github.davidmoten.odata.client.HttpService;
import com.github.davidmoten.odata.client.ODataEntityType;
import com.github.davidmoten.odata.client.ODataType;
import com.github.davidmoten.odata.client.Path;
import com.github.davidmoten.odata.client.RequestHeader;
import com.github.davidmoten.odata.client.RequestOptions;
import com.github.davidmoten.odata.client.SchemaInfo;
import com.github.davidmoten.odata.client.Serializer;
import com.github.davidmoten.odata.client.StreamProvider;

public final class RequestHelper {

    private static final int HTTP_OK_MIN = 200;
    private static final int HTTP_OK_MAX = 299;
    private static final String HTTPS = "https://";
    private static final String CONTENT_TYPE_APPLICATION_OCTET_STREAM = "application/octet-stream";

    private RequestHelper() {
        // prevent instantiation
    }

    /**
     * Returns the json from an HTTP GET of the url built from the contextPath and
     * options. In the case where the returned object is actually a sub-class of T
     * we lookup the sub-class from schemaInfo based on the namespaced type of the
     * return object.
     * 
     * @param <T>              return object type
     * @param contextPath      context and current path
     * @param returnCls        return class
     * @param options          request options
     * @param returnSchemaInfo schema to be used for lookup generated class of of
     *                         the returned object from the namespaced type
     * @return object hydrated from json
     */
    public static <T> T get(ContextPath contextPath, Class<T> returnCls, RequestOptions options,
            SchemaInfo returnSchemaInfo) {
        // build the url
        ContextPath cp = contextPath.addQueries(options.getQueries());

        List<RequestHeader> h = cleanAndSupplementRequestHeaders(options, "minimal", false);

        // get the response
        HttpResponse response = cp.context().service().get(cp.toUrl(), h);

        checkResponseCode(cp, response, HttpURLConnection.HTTP_OK);

        // deserialize
        // Though cls might be Class<Attachment> we might actually want to return a
        // sub-class like FileAttachment (which extends Attachment). This method returns
        // the actual sub-class by inspecting the json response.
        Class<? extends T> c = getSubClass(cp, returnSchemaInfo, returnCls, response.getText());
        // check if we need to deserialize into a subclass of T (e.g. return a
        // FileAttachment which is a subclass of Attachment)
        return cp.context().serializer().deserialize(response.getText(), c, contextPath, false);
    }

    public static void checkResponseCode(ContextPath cp, HttpResponse response,
            int expectedResponseCodeMin, int expectedResponseCodeMax) {
        if (response.getResponseCode() < expectedResponseCodeMin
                || response.getResponseCode() > expectedResponseCodeMax) {
            throw new ClientException("responseCode=" + response.getResponseCode() + " from url="
                    + cp.toUrl() + ", expectedResponseCode in [" + expectedResponseCodeMin + ", "
                    + expectedResponseCodeMax + "], message=\n" + response.getText());
        }
    }

    public static void checkResponseCode(ContextPath cp, HttpResponse response,
            int expectedResponseCode) {
        checkResponseCode(cp, response, expectedResponseCode, expectedResponseCode);
    }

    public static <T, S> T getWithParametricType(ContextPath contextPath, Class<T> cls,
            Class<S> parametricTypeClass, RequestOptions options, SchemaInfo schemaInfo) {
        // build the url
        ContextPath cp = contextPath.addQueries(options.getQueries());

        List<RequestHeader> h = cleanAndSupplementRequestHeaders(options, "minimal", false);

        // get the response
        HttpResponse response = cp.context().service().get(cp.toUrl(), h);

        checkResponseCode(cp, response, HttpURLConnection.HTTP_OK);

        // deserialize
        Class<? extends T> c = getSubClass(cp, schemaInfo, cls, response.getText());
        // check if we need to deserialize into a subclass of T (e.g. return a
        // FileAttachment which is a subclass of Attachment)
        return cp.context().serializer().deserializeWithParametricType(response.getText(), c,
                parametricTypeClass, contextPath, false);
    }

    // designed for saving a new entity and returning that entity
    public static <T extends ODataEntityType> T post(T entity, ContextPath contextPath,
            Class<T> cls, RequestOptions options, SchemaInfo schemaInfo) {
        return postAny(entity, contextPath, cls, options, schemaInfo);
    }

    public static void post(Map<String, Object> parameters, ContextPath contextPath,
            RequestOptions options) {

        String json = Serializer.INSTANCE.serialize(parameters);

        // build the url
        ContextPath cp = contextPath.addQueries(options.getQueries());
        List<RequestHeader> h = cleanAndSupplementRequestHeaders(options, "minimal", true);
        final String url = cp.toUrl();

        // get the response
        HttpService service = cp.context().service();
        final HttpResponse response = service.post(url, h, json);

        checkResponseCode(cp, response, HTTP_OK_MIN, HTTP_OK_MAX);
    }

    public static <T> T postAny(Object object, ContextPath contextPath, Class<T> responseClass,
            RequestOptions options, SchemaInfo responseSchemaInfo) {
        // build the url
        ContextPath cp = contextPath.addQueries(options.getQueries());

        String json = Serializer.INSTANCE.serialize(object);

        List<RequestHeader> h = cleanAndSupplementRequestHeaders(options, "minimal", true);

        // get the response
        HttpResponse response = cp.context().service().post(cp.toUrl(), h, json);

        // deserialize
        checkResponseCode(cp, response, HttpURLConnection.HTTP_CREATED);

        // deserialize
        Class<? extends T> c = getSubClass(cp, responseSchemaInfo, responseClass,
                response.getText());
        // check if we need to deserialize into a subclass of T (e.g. return a
        // FileAttachment which is a subclass of Attachment)
        return cp.context().serializer().deserialize(response.getText(), c, contextPath, false);
    }

    public static <T, S> T postAnyWithParametricType(Object object, ContextPath contextPath,
            Class<T> cls, Class<S> parametricTypeClass, RequestOptions options,
            SchemaInfo schemaInfo) {
        // build the url
        ContextPath cp = contextPath.addQueries(options.getQueries());

        String json = Serializer.INSTANCE.serialize(object);

        List<RequestHeader> h = cleanAndSupplementRequestHeaders(options, "minimal", true);

        // get the response
        HttpResponse response = cp.context().service().post(cp.toUrl(), h, json);

        checkResponseCode(cp, response, HttpURLConnection.HTTP_CREATED);

        // deserialize
        Class<? extends T> c = getSubClass(cp, schemaInfo, cls, response.getText());
        // check if we need to deserialize into a subclass of T (e.g. return a
        // FileAttachment which is a subclass of Attachment)
        return cp.context().serializer().deserializeWithParametricType(response.getText(), c,
                parametricTypeClass, contextPath, false);
    }

    public static <T extends ODataEntityType> T patch(T entity, ContextPath contextPath,
            RequestOptions options) {
        return patchOrPut(entity, contextPath, options, HttpMethod.PATCH);
    }

    public static <T extends ODataEntityType> void delete(ContextPath cp, RequestOptions options) {
        String url = cp.toUrl();
        List<RequestHeader> h = cleanAndSupplementRequestHeaders(options, "minimal", true);
        HttpResponse response = cp.context().service().delete(url, h);
        checkResponseCode(cp, response, HttpURLConnection.HTTP_NO_CONTENT);
    }

    public static <T extends ODataEntityType> T put(T entity, ContextPath contextPath,
            RequestOptions options) {
        return patchOrPut(entity, contextPath, options, HttpMethod.PUT);
    }

    private static <T extends ODataEntityType> T patchOrPut(T entity, ContextPath contextPath,
            RequestOptions options, HttpMethod method) {
        Preconditions.checkArgument(method == HttpMethod.PUT || method == HttpMethod.PATCH);
        String json = Serializer.INSTANCE.serializeChangesOnly(entity);

        // build the url
        ContextPath cp = contextPath.addQueries(options.getQueries());

        List<RequestHeader> h = cleanAndSupplementRequestHeaders(options, "minimal", true);

        final String url;
        String editLink = (String) entity.getUnmappedFields().get("@odata.editLink");
        // TODO get patch working when editLink present (does not work with MsGraph)
        if (editLink != null && false) {
            if (editLink.startsWith(HTTPS) || editLink.startsWith("http://")) {
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
        final HttpResponse response = service.submitWithContent(method, url, h, json);
        checkResponseCode(cp, response, HTTP_OK_MIN, HTTP_OK_MAX);
        return entity;
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<? extends T> getSubClass(ContextPath cp, SchemaInfo schemaInfo,
            Class<T> cls, String json) {
        Optional<String> namespacedType = cp.context().serializer().getODataType(json)
                .map(x -> x.substring(1));

        if (namespacedType.isPresent()) {
            return (Class<? extends T>) schemaInfo
                    .getClassFromTypeWithNamespace(namespacedType.get());
        } else {
            return cls;
        }
    }

    public static List<RequestHeader> cleanAndSupplementRequestHeaders(RequestOptions options,
            String contentTypeOdataMetadataValue, boolean isWrite) {
        // remove repeated headers

        List<RequestHeader> list = new ArrayList<>();
        list.add(new RequestHeader("OData-Version", "4.0"));
        if (isWrite) {
        list.add(RequestHeader.contentTypeJsonWithMetadata(contentTypeOdataMetadataValue));
        } 
        list.add(RequestHeader.ACCEPT_JSON);
        list.addAll(options.getRequestHeaders());

        // remove duplicates
        List<RequestHeader> list2 = new ArrayList<>();
        Set<RequestHeader> set = new HashSet<>();
        for (RequestHeader r : list) {
            if (!set.contains(r)) {
                list2.add(r);
            }
            set.add(r);
        }

        // remove overriden accept header
        if (list2.contains(RequestHeader.ACCEPT_JSON) && list2.stream()
                .filter(x -> x.isAcceptJsonWithMetadata()).findFirst().isPresent()) {
            list2.remove(RequestHeader.ACCEPT_JSON);
        }

        // only use the last accept with metadata request header
        Optional<RequestHeader> m = list2 //
                .stream() //
                .filter(x -> x.isAcceptJsonWithMetadata()) //
                .reduce((x, y) -> y);
        
        List<RequestHeader> list3 = list2.stream()
                .filter(x -> !x.isAcceptJsonWithMetadata() || !m.isPresent() || x.equals(m.get()))
                .collect(Collectors.toList());
        return list3;
    }

    public static InputStream getStream(ContextPath contextPath, RequestOptions options,
            String base64) {
        if (base64 != null) {
            return new ByteArrayInputStream(Base64.getDecoder().decode(base64));
        } else {
            ContextPath cp = contextPath.addQueries(options.getQueries());
            return contextPath.context().service().getStream(cp.toUrl(),
                    options.getRequestHeaders());
        }
    }

    // for HasStream case (only for entities, not for complexTypes)
    public static Optional<StreamProvider> createStream(ContextPath contextPath,
            ODataEntityType entity) {
        String editLink = (String) entity.getUnmappedFields().get("@odata.mediaEditLink");
        if (editLink == null) {
            editLink = (String) entity.getUnmappedFields().get("@odata.editLink");
        }
        String contentType = (String) entity.getUnmappedFields().get("@odata.mediaContentType");
        if (editLink == null && "false"
                .equals(contextPath.context().getProperty("attempt.stream.when.no.metadata"))) {
            return Optional.empty();
        } else {
            if (contentType == null) {
                contentType = CONTENT_TYPE_APPLICATION_OCTET_STREAM;
            }
            // TODO support relative editLink?
            Context context = contextPath.context();
            if (editLink == null) {
                editLink = contextPath.toUrl();
            }
            if (!editLink.startsWith(HTTPS)) {
                // TODO should use the base path from @odata.context field?
                editLink = concatenate(contextPath.context().service().getBasePath().toUrl(),
                        editLink);
            }
            if ("true".equals(contextPath.context().getProperty("modify.stream.edit.link"))) {
                // Bug fix for Microsoft Graph only?
                // When a collection is returned the editLink is terminated with the subclass if
                // the collection type has subclasses. For example when a collection of
                // Attachment (with full metadata) is requested the editLink of an individual
                // attachment may end in /itemAttachment to indicate the type of the attachment.
                // To get the $value download working we need to remove that type cast.
                int i = endsWith(editLink, "/" + entity.odataTypeName());
                if (i == -1) {
                    i = endsWith(editLink, "/" + entity.odataTypeName() + "/$value");
                }
                if (i == -1) {
                    i = endsWith(editLink, "/" + entity.odataTypeName() + "/%24value");
                }
                if (i != -1) {
                    editLink = editLink.substring(0, i);
                }
            }
            Path path = new Path(editLink, contextPath.path().style());
            if (!path.toUrl().endsWith("/$value")) {
                path = path.addSegment("$value");
            }
            return Optional.of(new StreamProvider( //
                    new ContextPath(context, path), //
                    RequestOptions.EMPTY, //
                    contentType, //
                    null));
        }
    }

    private static int endsWith(String a, String b) {
        if (a.endsWith(b)) {
            return a.length() - b.length();
        } else {
            return -1;
        }
    }

    // concatenate two url parts making sure there is a / delimiter
    private static String concatenate(String a, String b) {
        StringBuilder s = new StringBuilder();
        s.append(a);
        if (a.endsWith("/")) {
            if (b.startsWith("/")) {
                s.append(b, 1, b.length());
            } else {
                s.append(b);
            }
        } else {
            if (!b.startsWith("/")) {
                s.append('/');
            }
            s.append(b);
        }
        return s.toString();
    }

    public static Optional<StreamProvider> createStreamForEdmStream(ContextPath contextPath,
            ODataType item, String fieldName, String base64) {
        Preconditions.checkNotNull(fieldName);
        String readLink = (String) item.getUnmappedFields().get(fieldName + "@odata.mediaReadLink");
        String contentType = (String) item.getUnmappedFields()
                .get(fieldName + "@odata.mediaContentType");
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
