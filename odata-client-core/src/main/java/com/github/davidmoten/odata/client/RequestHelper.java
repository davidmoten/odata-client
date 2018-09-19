package com.github.davidmoten.odata.client;

import java.util.Map.Entry;

public final class RequestHelper {

    private RequestHelper() {
        // prevent instantiation
    }

    public static <T> T get(ContextPath contextPath, Class<T> cls, String id, RequestOptions options) {
        // build the url
        ContextPath cp = contextPath.addKeys(id);
        for (Entry<String, String> query : options.getQueries().entrySet()) {
            cp = cp.addQuery(query.getKey(), query.getValue());
        }
        // get the response
        ResponseGet response = cp.context().service().GET(cp.toUrl(), options.getRequestHeaders());

        // deserialize
        return cp.context().serializer().deserialize(response.getText(), cls, contextPath);
    }

}
