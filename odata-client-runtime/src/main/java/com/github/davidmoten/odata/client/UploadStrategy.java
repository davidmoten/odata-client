package com.github.davidmoten.odata.client;

import java.util.Optional;

public interface UploadStrategy<T> {

    T builder(ContextPath contextPath, ODataType entity, String fieldName);
    
    public static UploadStrategy<Optional<StreamUploader>> singleCall() {
        return UploadStrategySingleCall.INSTANCE;
    }

    public static UploadStrategy<Optional<StreamUploaderChunked>> chunked() {
        return UploadStrategyChunked.INSTANCE;
    }

}
