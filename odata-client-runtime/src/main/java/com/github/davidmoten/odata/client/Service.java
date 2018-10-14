package com.github.davidmoten.odata.client;

import java.util.Map;

public interface Service {

    HttpResponse GET(String url, Map<String, String> requestHeaders);

    HttpResponse PATCH(String url, Map<String, String> requestHeaders, String content);

    HttpResponse PUT(String url, Map<String, String> requestHeaders, String content);

    HttpResponse POST(String url, Map<String, String> requestHeaders, String content);

    Path getBasePath();

}
