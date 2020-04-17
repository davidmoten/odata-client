package com.github.davidmoten.odata.client;

import java.util.Optional;

import com.github.davidmoten.odata.client.internal.RequestHelper;

final class UploadStrategySingleCall implements UploadStrategy<Optional<StreamUploader>> {

    static final UploadStrategySingleCall INSTANCE = new UploadStrategySingleCall();

    private UploadStrategySingleCall() {
        // prevent instantiation
    }

    @Override
    public Optional<StreamUploader> builder(ContextPath contextPath, ODataType entity,
            String fieldName) {
        return RequestHelper.uploader(contextPath, entity, fieldName);
    }
}
