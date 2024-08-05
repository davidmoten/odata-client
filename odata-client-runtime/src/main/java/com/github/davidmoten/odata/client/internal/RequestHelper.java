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
import com.github.davidmoten.odata.client.HttpRequestOptions;
import com.github.davidmoten.odata.client.HttpResponse;
import com.github.davidmoten.odata.client.HttpService;
import com.github.davidmoten.odata.client.ODataEntityType;
import com.github.davidmoten.odata.client.ODataType;
import com.github.davidmoten.odata.client.Path;
import com.github.davidmoten.odata.client.Properties;
import com.github.davidmoten.odata.client.RequestHeader;
import com.github.davidmoten.odata.client.RequestOptions;
import com.github.davidmoten.odata.client.SchemaInfo;
import com.github.davidmoten.odata.client.Serializer;
import com.github.davidmoten.odata.client.StreamProvider;
import com.github.davidmoten.odata.client.StreamUploaderSingleCall;
import com.github.davidmoten.odata.client.UnmappedFields;

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
     * we lookup the sub-class from context schemaInfos based on the namespaced type
     * of the return object.
     * 
     * @param <T>         return object type
     * @param contextPath context and current path
     * @param returnCls   return class
     * @param options     request options
     * @return object hydrated from json
     */
    public static <T> T get(ContextPath contextPath, Class<T> returnCls, RequestOptions options) {
        // build the url
        ContextPath cp = contextPath.addQueries(options.getQueries());

        List<RequestHeader> h = cleanAndSupplementRequestHeaders(options, "minimal", false);

        // get the response
        HttpResponse response = cp.context().service().get(cp.toUrl(), h, options);

        checkResponseCode(cp, response, HttpURLConnection.HTTP_OK);

        // deserialize
        // Though cls might be Class<Attachment> we might actually want to return a
        // sub-class like FileAttachment (which extends Attachment). This method returns
        // the actual sub-class by inspecting the json response.
        Class<? extends T> c = getSubClass(cp, contextPath.context().schemas(), returnCls,
                response.getText());
        // check if we need to deserialize into a subclass of T (e.g. return a
        // FileAttachment which is a subclass of Attachment)
        return cp.context().serializer().deserialize(response.getText(), c, contextPath, false);
    }

    public static void checkResponseCode(String url, HttpResponse response,
            int expectedResponseCodeMin, int expectedResponseCodeMax) {
        if (response.getResponseCode() < expectedResponseCodeMin
                || response.getResponseCode() > expectedResponseCodeMax) {
            throw new ClientException(response.getResponseCode(),
                    "responseCode=" + response.getResponseCode() + " from url=" + url
                            + ", expectedResponseCode in [" + expectedResponseCodeMin + ", "
                            + expectedResponseCodeMax + "], message=\n" + response.getText());
        }
    }

    public static void checkResponseCodeOk(ContextPath cp, HttpResponse response) {
        checkResponseCode(cp, response, HTTP_OK_MIN, HTTP_OK_MAX);
    }

    private static void checkResponseCode(ContextPath cp, HttpResponse response,
            int expectedResponseCodeMin, int expectedResponseCodeMax) {
        checkResponseCode(cp.toUrl(), response, expectedResponseCodeMin, expectedResponseCodeMax);
    }

    public static void checkResponseCode(ContextPath cp, HttpResponse response,
            int expectedResponseCode) {
        checkResponseCode(cp, response, expectedResponseCode, expectedResponseCode);
    }

    public static <T, S> T getWithParametricType(ContextPath contextPath, Class<T> cls,
            Class<S> parametricTypeClass, RequestOptions options) {
        // build the url
        ContextPath cp = contextPath.addQueries(options.getQueries());

        List<RequestHeader> h = cleanAndSupplementRequestHeaders(options, "minimal", false);

        // get the response
        HttpResponse response = cp.context().service().get(cp.toUrl(), h, options);

        checkResponseCode(cp, response, HttpURLConnection.HTTP_OK);

        // deserialize
        String text = response.getText();
        Class<? extends T> c = getSubClass(cp, contextPath.context().schemas(), cls, text);
        // check if we need to deserialize into a subclass of T (e.g. return a
        // FileAttachment which is a subclass of Attachment)
        return cp.context().serializer().deserializeWithParametricType(text, c, parametricTypeClass,
                contextPath, false);
    }

    // designed for saving a new entity and returning that entity
    public static <T extends ODataEntityType> Optional<T> post(T entity, ContextPath contextPath,
            Class<T> cls, RequestOptions options) {
        return postAny(entity, contextPath, cls, options);
    }
    
    // designed for saving a new entity and returning that entity
    public static <T extends ODataEntityType> Optional<T> patch(T entity, ContextPath contextPath,
            Class<T> cls, RequestOptions options) {
        return patchAny(entity, contextPath, cls, options);
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
        final HttpResponse response = service.post(url, h, json, options);

        checkResponseCodeOk(cp, response);
    }
    
    public static <T> Optional<T> postAny(Object object, ContextPath contextPath, Class<T> responseClass,
            RequestOptions options) {
        return submitAny(HttpMethod.POST, object, contextPath, responseClass, options);
    }
    
    public static <T> Optional<T> patchAny(Object object, ContextPath contextPath, Class<T> responseClass,
            RequestOptions options) {
        return submitAny(HttpMethod.PATCH, object, contextPath, responseClass, options);
    }
    
    public static <T> Optional<T> submitAny(HttpMethod method, Object object, ContextPath contextPath, Class<T> responseClass,
            RequestOptions options) {
        // build the url
        ContextPath cp = contextPath.addQueries(options.getQueries());

        String json;
        if (object == null) {
            json = "";
        } else {
            json = Serializer.INSTANCE.serialize(object);
        }

        List<RequestHeader> h = cleanAndSupplementRequestHeaders(options, "minimal", true);

        // get the response
        HttpResponse response = cp.context().service().submit(method, cp.toUrl(), h, json, options);

        // TODO could be tightened to 201 for POST create but POST Action calls need to
        // accept any successful code
        checkResponseCodeOk(cp, response);

        String text = response.getText();
        if (text == null) {
            return Optional.empty();
        } else {
            // deserialize
            Class<? extends T> c = getSubClass(cp, contextPath.context().schemas(), responseClass,
                    text);
         // check if we need to deserialize into a subclass of T (e.g. return a
            // FileAttachment which is a subclass of Attachment)
            return Optional.of(cp.context().serializer().deserialize(text, c, contextPath, false));
        }
    }

    public static <T, S> T postAnyWithParametricType(Object object, ContextPath contextPath,
            Class<T> cls, Class<S> parametricTypeClass, RequestOptions options) {
        // build the url
        ContextPath cp = contextPath.addQueries(options.getQueries());

        String json = Serializer.INSTANCE.serialize(object);

        List<RequestHeader> h = cleanAndSupplementRequestHeaders(options, "minimal", true);

        // get the response
        HttpResponse response = cp.context().service().post(cp.toUrl(), h, json, options);

        checkResponseCodeOk(cp, response);

        String text = response.getText();

        // deserialize
        Class<? extends T> c = getSubClass(cp, contextPath.context().schemas(), cls, text);
        // check if we need to deserialize into a subclass of T (e.g. return a
        // FileAttachment which is a subclass of Attachment)
        return cp.context().serializer().deserializeWithParametricType(text, c, parametricTypeClass,
                contextPath, false);
    }

    public static <T extends ODataEntityType> T patch(T entity, ContextPath contextPath,
            RequestOptions options) {
        return patchOrPut(entity, contextPath, options, HttpMethod.PATCH);
    }

    public static <T extends ODataEntityType> void delete(ContextPath cp, RequestOptions options) {
        String url = cp.toUrl();
        List<RequestHeader> h = cleanAndSupplementRequestHeaders(options, "minimal", true);
        HttpResponse response = cp.context().service().delete(url, h, options);
        checkResponseCode(cp, response, HttpURLConnection.HTTP_NO_CONTENT);
    }

    public static <T extends ODataEntityType> T put(T entity, ContextPath contextPath,
            RequestOptions options) {
        return patchOrPut(entity, contextPath, options, HttpMethod.PUT);
    }

    @SuppressWarnings("unused")
    private static <T extends ODataEntityType> T patchOrPut(T entity, ContextPath contextPath,
            RequestOptions options, HttpMethod method) {
        Preconditions.checkArgument(method == HttpMethod.PUT || method == HttpMethod.PATCH);
        final String json;
        if (method == HttpMethod.PATCH) {
            json = Serializer.INSTANCE.serializeChangesOnly(entity);
        } else {
            json = Serializer.INSTANCE.serialize(entity);
        }

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
        final HttpResponse response = service.submit(method, url, h, json, options);
        checkResponseCodeOk(cp, response);
        // TODO is service returning the entity that we should use rather than the
        // original?
        return entity;
    }

    public static void put(ContextPath contextPath, RequestOptions options, InputStream in,
            int length) {
        send(HttpMethod.PUT, contextPath, options, in, length);
    }

    public static void send(HttpMethod method, ContextPath contextPath, RequestOptions options,
            InputStream in, int length) {
        List<RequestHeader> h = cleanAndSupplementRequestHeaders(options, "minimal", true);
        ContextPath cp = contextPath.addQueries(options.getQueries());
        HttpService service = cp.context().service();
        final HttpResponse response = service.submit(method, cp.toUrl(), h, in, length,
                options);
        checkResponseCodeOk(cp, response);
    }

    public static void addContentLengthHeader(List<RequestHeader> h, int length) {
        if (length > 0 && !h.stream().anyMatch(x -> x.name().equals("Content-Length"))) {
            h.add(RequestHeader.contentLength(length));
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<? extends T> getSubClass(ContextPath cp, List<SchemaInfo> schemas,
            Class<T> cls, String json) {
        Optional<String> namespacedType = cp.context().serializer().getODataType(json)
                .map(x -> x.startsWith("#") ? x.substring(1): x);
        if (namespacedType.isPresent()) {
            for (SchemaInfo schema : schemas) {
                Class<? extends T> c = (Class<? extends T>) schema
                        .getClassFromTypeWithNamespace(namespacedType.get());
                if (c != null) {
                    return c;
                }
            }
        }
        return cls;
    }

    public static List<RequestHeader> cleanAndSupplementRequestHeaders(
            List<RequestHeader> requestHeaders, String contentTypeOdataMetadataValue,
            boolean hasPayload) {

        List<RequestHeader> list = new ArrayList<>();
        list.add(RequestHeader.ODATA_VERSION);
        if (hasPayload) {
            if (!requestHeaders.stream().map(x -> x.name()).filter(x -> x.equals("Content-Type"))
                    .findFirst().isPresent()) {
                list.add(RequestHeader.CONTENT_TYPE_JSON);
            }
        }
        list.add(RequestHeader.ACCEPT_JSON);
        list.addAll(requestHeaders);

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
        if (list2.contains(RequestHeader.ACCEPT_JSON)
                && list2.stream().anyMatch(RequestHeader::isAcceptJsonWithMetadata)) {
            list2.remove(RequestHeader.ACCEPT_JSON);
        }

        // only use the last accept with metadata request header
        Optional<RequestHeader> m = list2 //
                .stream() //
                .filter(RequestHeader::isAcceptJsonWithMetadata) //
                .reduce((x, y) -> y);

        return list2.stream()
                .filter(x -> !x.isAcceptJsonWithMetadata() || !m.isPresent() || x.equals(m.get()))
                .collect(Collectors.toList());

    }

    public static List<RequestHeader> cleanAndSupplementRequestHeaders(RequestOptions options,
            String contentTypeOdataMetadataValue, boolean hasPayload) {
        return cleanAndSupplementRequestHeaders(options.getRequestHeaders(),
                contentTypeOdataMetadataValue, hasPayload);
    }

    public static InputStream getStream(ContextPath contextPath, RequestOptions options,
            String base64) {
        if (base64 != null) {
            return new ByteArrayInputStream(Base64.getDecoder().decode(base64));
        } else {
            ContextPath cp = contextPath.addQueries(options.getQueries());
            return contextPath.context().service().getStream(cp.toUrl(),
                    options.getRequestHeaders(), options);
        }
    }

    // for HasStream case (only for entities, not for complexTypes)
    public static Optional<StreamProvider> createStream(ContextPath contextPath,
            ODataEntityType entity) {
        String editLink;
        String contentType;
        if (entity == null) {
            editLink = null;
            contentType = null;
        } else {
            editLink = (String) entity.getUnmappedFields().get("@odata.mediaEditLink");
            if (editLink == null) {
                editLink = (String) entity.getUnmappedFields().get("@odata.editLink");
            }
            contentType = (String) entity.getUnmappedFields().get("@odata.mediaContentType");
        }
        if (editLink == null && contextPath.context()
                .propertyIsFalse(Properties.ATTEMPT_STREAM_WHEN_NO_METADATA)) {
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
            if (contextPath.context().propertyIsTrue(Properties.MODIFY_STREAM_EDIT_LINK)
                    && entity != null) {
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
        if (readLink == null && base64 == null) {
           return Optional.empty();
        } else {
            if (contentType == null) {
                contentType = CONTENT_TYPE_APPLICATION_OCTET_STREAM;
            }
            // TODO support relative editLink?
            Context context = contextPath.context();
            // path won't be used if readLink is null because base64 is not null
            // $value is not appended for a stream property
            Path path = new Path(readLink, contextPath.path().style());
            return Optional.of(new StreamProvider( //
                    new ContextPath(context, path), //
                    RequestOptions.EMPTY, //
                    contentType, //
                    base64));
        }
    }

    public static Optional<StreamUploaderSingleCall> uploader(ContextPath contextPath,
            ODataType item, String fieldName, HttpMethod method) {
        Preconditions.checkNotNull(fieldName);
        String editLink = (String) item.getUnmappedFields().get(fieldName + "@odata.mediaEditLink");
        String contentType = (String) item.getUnmappedFields()
                .get(fieldName + "@odata.mediaContentType");
        if (editLink == null) {
            return Optional.empty();
        } else {
            // TODO support relative editLink?
            Context context = contextPath.context();
            if (contentType == null) {
                contentType = CONTENT_TYPE_APPLICATION_OCTET_STREAM;
            }
            Path path = new Path(editLink, contextPath.path().style());
            return Optional.of(new StreamUploaderSingleCall(new ContextPath(context, path),
                    contentType, method));
        }
    }

    public static void putChunk(HttpService service, String url, InputStream in,
            List<RequestHeader> requestHeaders, long startByte, long finishByte, long size,
            HttpRequestOptions options) {
        sendChunk(HttpMethod.PUT, service, url, in, requestHeaders, startByte, finishByte, size,
                options);
    }

    public static void sendChunk(HttpMethod method, HttpService service, String url, InputStream in,
            List<RequestHeader> requestHeaders, long startByte, long finishByte, long size,
            HttpRequestOptions options) {
        List<RequestHeader> h = new ArrayList<RequestHeader>(requestHeaders);
        h.add(RequestHeader.create("Content-Range",
                "bytes " + startByte + "-" + (finishByte - 1) + "/" + size));
        HttpResponse response = service.put(url, h, in, (int) (finishByte - startByte), options);
        checkResponseCode(url, response, 200, 204);
    }

    public static Optional<Object> getValue(UnmappedFields unmappedFields, String name) {
        if (unmappedFields == null) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(unmappedFields.get(name));
        }
    }

}
