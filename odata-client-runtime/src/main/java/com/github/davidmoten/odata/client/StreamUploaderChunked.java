package com.github.davidmoten.odata.client;

public final class StreamUploaderChunked {

    private final ContextPath contextPath;
    private final ODataType entity;
    private final String fieldName;

    StreamUploaderChunked(ContextPath contextPath, ODataType entity, String fieldName) {
        this.contextPath = contextPath;
        this.entity = entity;
        this.fieldName = fieldName;
    }

}
