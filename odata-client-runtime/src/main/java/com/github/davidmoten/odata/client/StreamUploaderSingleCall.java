package com.github.davidmoten.odata.client;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.github.davidmoten.odata.client.internal.RequestHelper;

public final class StreamUploaderSingleCall implements StreamUploader<StreamUploaderSingleCall> {

    private final ContextPath contextPath;
    private final Map<String, String> queries;
    private final List<RequestHeader> requestHeaders;
	private Optional<Long> connectTimeoutMs = Optional.empty();
	private Optional<Long> readTimeoutMs = Optional.empty();

    public StreamUploaderSingleCall(ContextPath contextPath, String contentType) {
        this.contextPath = contextPath;
        this.queries = new HashMap<>();
        this.requestHeaders = new ArrayList<>();
        requestHeaders.add(RequestHeader.contentType(contentType));
    }

    public StreamUploaderSingleCall requestHeader(String name, String value) {
        requestHeaders.add(RequestHeader.create(name, value));
        return this;
    }
    
    public StreamUploaderSingleCall connectTimeout(long duration, TimeUnit unit) {
    	this.connectTimeoutMs = Optional.of(unit.toMillis(duration));
    	return this;
    }
    
    public StreamUploaderSingleCall readTimeout(long duration, TimeUnit unit) {
    	this.readTimeoutMs = Optional.of(unit.toMillis(duration));
    	return this;
    }
    
    public void upload(byte[] bytes) {
        upload(new ByteArrayInputStream(bytes), bytes.length);
    }
    
    public void uploadUtf8(String text) {
        upload(text.getBytes(StandardCharsets.UTF_8));
    }
    
    public void upload(InputStream in, int length) {
        requestHeaders.add(RequestHeader.contentRange(0, length - 1, length));
        RequestHelper.put(contextPath, RequestOptions.create(queries, requestHeaders, connectTimeoutMs, readTimeoutMs), in, length);
    }
    
    public void upload(InputStream in, int length, UploadListener listener, int reportingChunkSize) {
        upload(new InputStreamWithProgress(in, reportingChunkSize, listener), length);
    }

    public void upload(InputStream in, int length, UploadListener listener) {
        upload(in, length, listener, 8192);
    }

}
