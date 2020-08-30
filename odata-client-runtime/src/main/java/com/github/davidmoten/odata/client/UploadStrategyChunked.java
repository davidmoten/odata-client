package com.github.davidmoten.odata.client;

import java.util.Optional;

public final class UploadStrategyChunked
        implements UploadStrategy<StreamUploaderChunked> {

    static final UploadStrategyChunked INSTANCE = new UploadStrategyChunked();

    private UploadStrategyChunked() {
        // prevent instantiation
    }

    @Override
    public Optional<StreamUploaderChunked> builder(ContextPath contextPath, ODataType entity,
            String fieldName, HttpMethod method) {
        // TODO inspect metadata to see if can upload stream
        String contentType = "application/octet-stream";
        return Optional.of(new StreamUploaderChunked(contextPath, contentType, method));
    }

}
