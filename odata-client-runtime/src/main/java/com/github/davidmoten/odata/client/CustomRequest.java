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

    public CustomRequest(Context context) {
        this.context = context;
    }

    public String getString(String url, RequestOptions options, RequestHeader... headers) {
        return context.service().getStringUtf8(url, Arrays.asList(headers), options);
    }

    public InputStream getStream(String url, RequestOptions options, RequestHeader... headers) {
        return context.service().getStream(url, Arrays.asList(headers), options);
    }

    public <T> T get(String url, Class<T> responseCls, SchemaInfo responseSchemaInfo, HttpRequestOptions options,
            RequestHeader... headers) {
        UrlInfo info = getInfo(context, url, headers, options);
        return RequestHelper.get(info.contextPath, responseCls, info, responseSchemaInfo);
    }

    public <T extends ODataEntityType> void post(String url, Class<T> contentClass, T content,
            SchemaInfo schemaInfo, HttpRequestOptions options, RequestHeader... headers) {
        UrlInfo info = getInfo(context, url, headers, options);
        RequestHelper.post(content, info.contextPath, contentClass, info, schemaInfo);
    }

    public <T> T post(String url, Object content, Class<T> responseClass,
            SchemaInfo responseSchemaInfo, HttpRequestOptions options, RequestHeader... headers) {
        UrlInfo info = getInfo(context, url, headers, options);
        return RequestHelper.postAny(context, info.contextPath, responseClass, info,
                responseSchemaInfo);
    }

    public void postJson(String url, String contentJson, RequestOptions options, RequestHeader... headers) {
        UrlInfo info = getInfo(context, url, headers, options);
        context.service().post(url, info.requestHeaders, contentJson, options);
    }

    public String postJsonReturnsJson(String url, String contentJson, RequestOptions options, RequestHeader... headers) {
        UrlInfo info = getInfo(context, url, headers, options);
        HttpResponse response = context.service().post(url, info.requestHeaders, contentJson, options);
        RequestHelper.checkResponseCode(info.contextPath, response, 200, 299);
        return response.getText();
    }

    private static UrlInfo getInfo(Context context, String url, RequestHeader[] requestHeaders, HttpRequestOptions options) {
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
