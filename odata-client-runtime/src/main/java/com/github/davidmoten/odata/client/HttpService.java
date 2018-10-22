package com.github.davidmoten.odata.client;

import java.io.InputStream;
import java.util.List;
import java.util.function.Function;

import com.github.davidmoten.odata.client.internal.DefaultHttpService;

public interface HttpService extends AutoCloseable {

    HttpResponse get(String url, List<RequestHeader> requestHeaders);

    HttpResponse patch(String url, List<RequestHeader> requestHeaders, String content);

    HttpResponse put(String url, List<RequestHeader> requestHeaders, String content);

    HttpResponse post(String url, List<RequestHeader> requestHeaders, String content);

    HttpResponse delete(String url, List<RequestHeader> requestHeaders);

    InputStream getStream(String url, List<RequestHeader> requestHeaders);

    Path getBasePath();

    public static HttpService createDefaultService(Path path,
            Function<List<RequestHeader>, List<RequestHeader>> requestHeadersModifier) {
        return new DefaultHttpService(path, requestHeadersModifier);
    }

}
