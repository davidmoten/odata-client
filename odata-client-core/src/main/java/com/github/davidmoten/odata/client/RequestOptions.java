package com.github.davidmoten.odata.client;

import java.util.List;
import java.util.Map;

public interface RequestOptions {

    Map<String, String> getRequestHeaders();

    List<String> getQueries();

}
