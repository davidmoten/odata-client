package com.github.davidmoten.odata.client;

import java.util.Map;

public interface Service {

    HttpResponse GET(String url, Map<String, String> requestHeaders);

    HttpResponse PATCH(String url, Map<String, String> requestHeaders);

    Path getBasePath();

}
