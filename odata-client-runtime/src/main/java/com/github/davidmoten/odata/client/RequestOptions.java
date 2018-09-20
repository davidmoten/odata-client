package com.github.davidmoten.odata.client;

import java.util.Map;

public interface RequestOptions {

    Map<String, String> getRequestHeaders();

    Map<String, String> getQueries();

}
