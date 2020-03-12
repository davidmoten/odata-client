package com.github.davidmoten.odata.client;

import java.io.InputStream;

public final class StreamUploader {
    
    private final ContextPath contextPath;
    private final String fieldName;

    public StreamUploader(ContextPath contextPath, String fieldName) {
        this.contextPath = contextPath;
        this.fieldName = fieldName;
    }

    public void upload(InputStream in, UploadListener listener) {
        //TODO
    }
    
}
