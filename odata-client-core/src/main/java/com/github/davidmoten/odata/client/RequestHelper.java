package com.github.davidmoten.odata.client;

public final class RequestHelper {

    private RequestHelper() {
        // prevent instantiation
    }

    public static <T extends ODataEntity> T get(ContextPath contextPath, Class<T> cls, RequestOptions options) {
        // build the url
        ContextPath cp = contextPath.addQueries(options.getQueries());
        // get the response
        ResponseGet response = cp.context().service().GET(cp.toUrl(), options.getRequestHeaders());
        // deserialize
        return cp.context().serializer().deserialize(response.getText(), cls, contextPath);
    }

    public static <T extends ODataEntity> CollectionPageEntity<T> getCollection(ContextPath contextPath, Class<T> cls,
            RequestOptions options) {
        // build the url
        ContextPath cp = contextPath.addQueries(options.getQueries());
        // get the response
        ResponseGet response = cp.context().service().GET(cp.toUrl(), options.getRequestHeaders());
        // deserialize
        // return cp.context().serializer().deserialize(response.getText(), cls,
        // contextPath);
        return null;
    }

}
