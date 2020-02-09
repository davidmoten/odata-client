package com.github.davidmoten.odata.client;

import java.util.Map;

public interface ODataType {
    
    String odataTypeName();

    Map<String, Object> getUnmappedFields();
    
    void postInject(boolean addKeysToContextPath);

}
