package com.github.davidmoten.odata.client;

public final class CustomRequest {

    private final Context context;

    public CustomRequest(Context context) {
        this.context = context;
    }

    public <T> T get(String url, Class<T> responseCls, RequestHeader... headers) {
        // TODO implement
        throw new UnsupportedOperationException("not implemented yet");
    }

    public <T> String getJson(String url, RequestHeader... headers) {
        // TODO implement
        throw new UnsupportedOperationException("not implemented yet");
    }

    public <T> void post(String url, Class<T> contentClass, T content, RequestHeader... headers) {
        // TODO implement
        throw new UnsupportedOperationException("not implemented yet");
    }

    public <T> void postJson(String url, String contentJson, RequestHeader... headers) {
        // TODO implement
        throw new UnsupportedOperationException("not implemented yet");
    }

    public String postJsonReturnsJson(String url, String contentJson, RequestHeader... headers) {
        // TODO implement
        throw new UnsupportedOperationException("not implemented yet");
    }

    public <T, S> T post(String url, Class<T> contentClass, T content, Class<S> responseClass,
            RequestHeader... headers) {
        // TODO implement
        throw new UnsupportedOperationException("not implemented yet");
    }

}
