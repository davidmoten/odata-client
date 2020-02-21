package com.github.davidmoten.odata.client;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.http.client.utils.URLEncodedUtils;

import com.github.davidmoten.odata.client.internal.RequestHelper;

public final class CustomRequest {

    private final Context context;

    public CustomRequest(Context context) {
        this.context = context;
    }

    public <T> T get(String url, Class<T> responseCls, SchemaInfo schemaInfo, RequestHeader... headers) {
        UrlInfo info = getInfo(context, url, headers);
        return RequestHelper.get(info.contextPath, responseCls, info, schemaInfo);
    }

    public <T> String getJson(String url, RequestHeader... headers) {
        // TODO implement
        throw new UnsupportedOperationException("not implemented yet");
    }

    public <T> void post(String url, Class<T> contentClass, T content, RequestHeader... headers) {
        // TODO implement
        throw new UnsupportedOperationException("not implemented yet");
    }

    public <T> void postJson(String url, String contentJson, RequestHeader... headers) {
        // TODO implement
        throw new UnsupportedOperationException("not implemented yet");
    }

    public String postJsonReturnsJson(String url, String contentJson, RequestHeader... headers) {
        // TODO implement
        throw new UnsupportedOperationException("not implemented yet");
    }

    public <T, S> T post(String url, Class<T> contentClass, T content, Class<S> responseClass,
            RequestHeader... headers) {
        // TODO implement
        throw new UnsupportedOperationException("not implemented yet");
    }

    private static UrlInfo getInfo(Context context, String url, RequestHeader[] requestHeaders) {
        final String urlPath;
        final String urlQuery;
        int i = url.indexOf('?');
        if (i == -1) {
            urlPath = url;
            urlQuery = "";
        } else {
            urlPath = url.substring(0, i);
            urlQuery = url.substring(i, url.length());
        }
        Path path = new Path(urlPath, context.service().getBasePath().style());
        ContextPath contextPath = new ContextPath(context, path);
        Map<String, String> queries = URLEncodedUtils //
                .parse(urlQuery, StandardCharsets.UTF_8) //
                .stream() //
                .peek(x -> System.out.println("pair=" + x)) //
                .collect(Collectors.toMap(pair -> pair.getName(), pair -> pair.getValue()));
        return new UrlInfo(contextPath, queries, Arrays.asList(requestHeaders));
    }

    private static final class UrlInfo implements RequestOptions {
        final ContextPath contextPath;
        final List<RequestHeader> requestHeaders;
        final Map<String, String> queries;

        UrlInfo(ContextPath contextPath, Map<String, String> queries, List<RequestHeader> requestHeaders) {
            this.contextPath = contextPath;
            this.queries = queries;
            this.requestHeaders = requestHeaders;
        }

        @Override
        public List<RequestHeader> getRequestHeaders() {
            return requestHeaders;
        }

        @Override
        public Map<String, String> getQueries() {
            return queries;
        }
    }
}
