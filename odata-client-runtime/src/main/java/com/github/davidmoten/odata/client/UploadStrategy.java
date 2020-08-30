package com.github.davidmoten.odata.client;

import java.util.Optional;

public interface UploadStrategy<T extends StreamUploader<T>> {

    Optional<T> builder(ContextPath contextPath, ODataType entity, String fieldName, HttpMethod method);
    
    static UploadStrategy<StreamUploaderSingleCall> singleCall() {
        return UploadStrategySingleCall.INSTANCE;
    }

    static UploadStrategy<StreamUploaderChunked> chunked() {
        return UploadStrategyChunked.INSTANCE;
    }

}
