package com.github.davidmoten.odata.client;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import com.github.davidmoten.odata.client.internal.RequestHelper;

public final class CustomRequest {

    private final Context context;
    private final boolean relativeUrls;

    public CustomRequest(Context context, boolean relativeUrls) {
        this.context = context;
        this.relativeUrls = relativeUrls;
    }
    
    private String toAbsoluteUrl(String url) {
        if (relativeUrls) {
            if (url.startsWith("https://")) {
                return url;
            } else {
                String base = context.service().getBasePath().toUrl();
                String a;
                if (base.endsWith("/")) {
                    a = base;
                } else {
                    a = base + "/";
                }
                String b;
                if (url.startsWith("/")) {
                    b = url.substring(1);
                } else {
                    b = url;
                }
                return a + b;
            }
        } else {
            return url;
        }
    }
    
    public CustomRequest withRelativeUrls() {
        return new CustomRequest(context, true);
    }
    
    public CustomRequest withRelativeUrls(boolean value) {
        return new CustomRequest(context, value);
    }
    
    public String getString(String url, RequestOptions options, RequestHeader... headers) {
        return context.service().getStringUtf8(toAbsoluteUrl(url), Arrays.asList(headers), options);
    }

    public InputStream getStream(String url, RequestOptions options, RequestHeader... headers) {
        return context.service().getStream(toAbsoluteUrl(url), Arrays.asList(headers), options);
    }

    public <T> T get(String url, Class<T> responseCls, HttpRequestOptions options,
            RequestHeader... headers) {
        UrlInfo info = getInfo(context, toAbsoluteUrl(url), headers, options);
        return RequestHelper.get(info.contextPath, responseCls, info);
    }

    public <T extends ODataEntityType> void post(String url, Class<T> contentClass, T content,
            HttpRequestOptions options, RequestHeader... headers) {
        UrlInfo info = getInfo(context, toAbsoluteUrl(url), headers, options);
        RequestHelper.post(content, info.contextPath, contentClass, info);
    }

    public <T> T post(String url, Object content, Class<T> responseClass,
            HttpRequestOptions options, RequestHeader... headers) {
        UrlInfo info = getInfo(context, toAbsoluteUrl(url), headers, options);
        return RequestHelper.postAny(content, info.contextPath, responseClass, info);
    }

    public void postString(String url, String content, RequestOptions options,
            RequestHeader... headers) {
        submitString(HttpMethod.POST, url, content, options, headers);
    }

    public String postStringReturnsString(String url, String content, RequestOptions options,
            RequestHeader... headers) {
        return submitStringReturnsString(HttpMethod.POST, url, content, options, headers);
    }
    
    public void patchString(String url, String content, RequestOptions options,
            RequestHeader... headers) {
        submitString(HttpMethod.PATCH, url, content, options, headers);
    }

    public String patchStringReturnsString(String url, String content, RequestOptions options,
            RequestHeader... headers) {
        return submitStringReturnsString(HttpMethod.PATCH, url, content, options, headers);
    }

    public void putString(String url, String content, RequestOptions options,
            RequestHeader... headers) {
        submitString(HttpMethod.PUT, url, content, options, headers);
    }

    public String putStringReturnsString(String url, String content, RequestOptions options,
            RequestHeader... headers) {
        return submitStringReturnsString(HttpMethod.PUT, url, content, options, headers);
    }

    public void submitString(HttpMethod method, String url, String content, RequestOptions options,
            RequestHeader... headers) {
        String absoluteUrl = toAbsoluteUrl(url);
        UrlInfo info = getInfo(context, absoluteUrl, headers, options);
        context.service().submitWithContent(method, absoluteUrl, info.requestHeaders, content, options);
    }

    public String submitStringReturnsString(HttpMethod method, String url, String content, RequestOptions options,
            RequestHeader... headers) {
        String absoluteUrl = toAbsoluteUrl(url);
        UrlInfo info = getInfo(context, absoluteUrl, headers, options);
        HttpResponse response = context.service().submitWithContent(method, absoluteUrl, info.requestHeaders, content,
                options);
        RequestHelper.checkResponseCodeOk(info.contextPath, response);
        return response.getText();
    }
    
    private static UrlInfo getInfo(Context context, String url, RequestHeader[] requestHeaders,
            HttpRequestOptions options) {
        final String urlPath;
        final String urlQuery;
        int i = url.indexOf('?');
        if (i == -1) {
            urlPath = url;
            urlQuery = "";
        } else {
            urlPath = url.substring(0, i);
            urlQuery = url.substring(i);
        }
        Path path = new Path(urlPath, context.service().getBasePath().style());
        ContextPath contextPath = new ContextPath(context, path);
        Map<String, String> queries = URLEncodedUtils //
                .parse(urlQuery, StandardCharsets.UTF_8) //
                .stream() //
                .collect(Collectors.toMap(NameValuePair::getName, NameValuePair::getValue));
        return new UrlInfo(contextPath, queries, Arrays.asList(requestHeaders), options);
    }

    private static final class UrlInfo implements RequestOptions {
        final ContextPath contextPath;
        final List<RequestHeader> requestHeaders;
        final Map<String, String> queries;
        final HttpRequestOptions options;

        UrlInfo(ContextPath contextPath, Map<String, String> queries,
                List<RequestHeader> requestHeaders, HttpRequestOptions options) {
            this.contextPath = contextPath;
            this.queries = queries;
            this.requestHeaders = requestHeaders;
            this.options = options;
        }

        @Override
        public List<RequestHeader> getRequestHeaders() {
            return requestHeaders;
        }

        @Override
        public Map<String, String> getQueries() {
            return queries;
        }

        @Override
        public Optional<String> getUrlOverride() {
            return Optional.empty();
        }

        @Override
        public Optional<Long> requestConnectTimeoutMs() {
            return options.requestConnectTimeoutMs();
        }

        @Override
        public Optional<Long> requestReadTimeoutMs() {
            return options.requestReadTimeoutMs();
        }
    }
}
