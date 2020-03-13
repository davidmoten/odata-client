package com.github.davidmoten.odata.client;

import java.util.Optional;

import com.github.davidmoten.odata.client.internal.RequestHelper;

final class UploadStrategies {

    static final UploadStrategy<Optional<StreamUploader>> SINGLE_CALL = //
            new UploadStrategy<Optional<StreamUploader>>() {

                @Override
                public Optional<StreamUploader> builder(ContextPath contextPath, ODataType entity,
                        String fieldName) {
                    return RequestHelper.uploader(contextPath, entity, fieldName);
                }
            };
}
