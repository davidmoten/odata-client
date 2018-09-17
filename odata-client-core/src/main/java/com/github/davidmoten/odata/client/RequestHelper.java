package com.github.davidmoten.odata.client;

public final class RequestHelper {

    private RequestHelper() {
        // prevent instantiation
    }

    public static <T> T get(ContextPath contextPath, Class<T> cls, String id, RequestOptions options) {
        // build the url
        ContextPath cp = contextPath.addKeys(id);
        for (String query : options.getQueries()) {
            cp = cp.addQuery(query);
        }
        // get the response
        ResponseGet response = cp.context().service().getResponseGET(cp.toUrl(), options.getRequestHeaders());

        // deserialize
        return cp.context().serializer().deserialize(response.getText(), cls, contextPath);
    }

}
