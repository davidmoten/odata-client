package com.github.davidmoten.odata.client;

import java.util.Optional;

import com.github.davidmoten.odata.client.internal.RequestHelper;

final class UploadStrategySingleCall implements UploadStrategy<StreamUploaderSingleCall> {

    static final UploadStrategySingleCall INSTANCE = new UploadStrategySingleCall();

    private UploadStrategySingleCall() {
        // prevent instantiation
    }

    @Override
    public Optional<StreamUploaderSingleCall> builder(ContextPath contextPath, ODataType entity,
            String fieldName, HttpMethod method) {
        return RequestHelper.uploader(contextPath, entity, fieldName, method);
    }
}
