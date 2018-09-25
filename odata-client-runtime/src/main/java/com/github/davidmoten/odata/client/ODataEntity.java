package com.github.davidmoten.odata.client;

import java.util.Map;

import com.github.davidmoten.odata.client.internal.ChangedFields;

public interface ODataEntity {
     Map<String,String> getUnmappedFields();
     
     ChangedFields getChangedFields();
}
