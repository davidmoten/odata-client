package com.github.davidmoten.odata.client;

import java.util.Map;

public final class Requests {

    private Requests() {
        // prevent instantiation
    }

    public static <T> T get(ContextPath contextPath, Class<T> cls, String id, Map<String, String> requestHeaders) {
        ContextPath cp = contextPath.addKeys(id);
        ResponseGet response = cp.context().service().getResponseGET(cp.toUrl(), requestHeaders);
        return cp.context().serializer().deserialize(response.getText(), cls);
    }

}
