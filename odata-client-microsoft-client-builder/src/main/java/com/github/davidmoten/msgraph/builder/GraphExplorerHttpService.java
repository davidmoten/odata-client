package com.github.davidmoten.msgraph.builder;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.github.davidmoten.odata.client.HttpRequestOptions;
import com.github.davidmoten.odata.client.HttpResponse;
import com.github.davidmoten.odata.client.HttpService;
import com.github.davidmoten.odata.client.Path;
import com.github.davidmoten.odata.client.RequestHeader;

public final class GraphExplorerHttpService implements HttpService {
    
    private final HttpService s;

    public GraphExplorerHttpService(HttpService s) {
        this.s = s;
    }

    private static String convert(String url) {
        String x;
        try {
            x = URLEncoder.encode(url, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return "https://proxy.apisandbox.msdn.microsoft.com/svc?url=" + x;
    }
    
    @Override
    public void close() throws Exception {
        s.close();
    }

    @Override
    public HttpResponse get(String url, List<RequestHeader> requestHeaders, HttpRequestOptions options) {
        return s.get(convert(url), requestHeaders, options);
    }

    @Override
    public HttpResponse patch(String url, List<RequestHeader> requestHeaders,
            InputStream content, int length, HttpRequestOptions options) {
        return s.patch(convert(url), requestHeaders, content, length, options);
    }

    @Override
    public HttpResponse put(String url, List<RequestHeader> requestHeaders,
            InputStream content, int length, HttpRequestOptions options) {
        return s.put(convert(url), requestHeaders, content, length, options);
    }

    @Override
    public HttpResponse post(String url, List<RequestHeader> requestHeaders,
            InputStream content, int length, HttpRequestOptions options) {
        return s.post(convert(url), requestHeaders, content, length, options);
    }

    @Override
    public HttpResponse delete(String url, List<RequestHeader> requestHeaders, HttpRequestOptions options) {
        return s.delete(convert(url), requestHeaders, options);
    }

    @Override
    public InputStream getStream(String url, List<RequestHeader> requestHeaders, HttpRequestOptions options) {
        return s.getStream(convert(url), requestHeaders, options);
    }

    @Override
    public Path getBasePath() {
        return s.getBasePath();
    }
    
}