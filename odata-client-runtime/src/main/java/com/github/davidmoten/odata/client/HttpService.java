package com.github.davidmoten.odata.client;

import java.util.List;
import java.util.function.Function;

import com.github.davidmoten.odata.client.internal.DefaultHttpService;

public interface HttpService {

    HttpResponse GET(String url, List<RequestHeader> requestHeaders);

    HttpResponse PATCH(String url, List<RequestHeader> requestHeaders, String content);

    HttpResponse PUT(String url, List<RequestHeader> requestHeaders, String content);

    HttpResponse POST(String url, List<RequestHeader> requestHeaders, String content);

    HttpResponse DELETE(String url, List<RequestHeader> requestHeaders);

    Path getBasePath();

    public static HttpService createDefaultService(Path path,
            Function<List<RequestHeader>, List<RequestHeader>> requestHeadersModifier) {
        return new DefaultHttpService(path, requestHeadersModifier);
    }

}
