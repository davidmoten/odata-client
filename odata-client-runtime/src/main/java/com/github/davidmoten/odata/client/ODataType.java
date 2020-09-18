package com.github.davidmoten.odata.client;

public interface ODataType {

    String odataTypeName();

    UnmappedFields getUnmappedFields();

    void postInject(boolean addKeysToContextPath);

}
