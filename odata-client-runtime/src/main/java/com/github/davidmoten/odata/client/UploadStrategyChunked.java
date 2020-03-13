package com.github.davidmoten.odata.client;

import java.util.Optional;

public final class UploadStrategyChunked
        implements UploadStrategy<Optional<StreamUploaderChunked>> {

    static final UploadStrategyChunked INSTANCE = new UploadStrategyChunked();

    private UploadStrategyChunked() {
        // prevent instantiation
    }

    @Override
    public Optional<StreamUploaderChunked> builder(ContextPath contextPath, ODataType entity,
            String fieldName) {
        return Optional.of(new StreamUploaderChunked(contextPath, entity, fieldName));
    }

}
