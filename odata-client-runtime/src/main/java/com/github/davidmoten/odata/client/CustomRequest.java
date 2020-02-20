package com.github.davidmoten.odata.client;

public final class CustomRequest {

    private final Context context;

    public CustomRequest(Context context) {
        this.context = context;
    }

    public <T> T get(String url, Class<T> responseCls, RequestHeader... headers) {
        return null;
    }
    
    public <T> String getJson(String url, RequestHeader... headers) {
        return null;
    }

    public <T> void post(String url, Class<T> contentClass, T content, RequestHeader... headers) {
    }
    
    public <T> void postJson(String url, String contentJson, RequestHeader... headers) {
    }
    
    public String postJsonReturnsJson(String url, String contentJson, RequestHeader... headers) {
        return null;
    }
    
    public <T, S> T post(String url, Class<T> contentClass, T content, Class<S> responseClass, RequestHeader... headers) {
        return null;
    }

}
