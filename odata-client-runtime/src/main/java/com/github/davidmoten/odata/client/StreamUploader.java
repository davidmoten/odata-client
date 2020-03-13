package com.github.davidmoten.odata.client;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.davidmoten.odata.client.internal.RequestHelper;

public final class StreamUploader {

    private final ContextPath contextPath;
    private final Map<String, String> queries;
    private final List<RequestHeader> requestHeaders;

    public StreamUploader(ContextPath contextPath, String contentType) {
        this.contextPath = contextPath;
        this.queries = new HashMap<>();
        this.requestHeaders = new ArrayList<>();
        requestHeaders.add(RequestHeader.create("Content-Type", contentType));
    }

    // TODO add request headers customization

    public StreamUploader requestHeader(String name, String value) {
        requestHeaders.add(RequestHeader.create(name, value));
        return this;
    }

    public void upload(InputStream in, UploadListener listener) {
        RequestHelper.put(contextPath, RequestOptions.create(queries, requestHeaders), in);
    }

}
