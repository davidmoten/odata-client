package com.github.davidmoten.odata.client;

import java.util.Map;

import com.github.davidmoten.odata.client.internal.DefaultService;

public interface Service {

    HttpResponse GET(String url, Map<String, String> requestHeaders);

    HttpResponse PATCH(String url, Map<String, String> requestHeaders, String content);

    HttpResponse PUT(String url, Map<String, String> requestHeaders, String content);

    HttpResponse POST(String url, Map<String, String> requestHeaders, String content);

    HttpResponse DELETE(String url, Map<String, String> requestHeaders);

    Path getBasePath();

    public static Service create(Path path) {
        return new DefaultService(path);
    }

}
