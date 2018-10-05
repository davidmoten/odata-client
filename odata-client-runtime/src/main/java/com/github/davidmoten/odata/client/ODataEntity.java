package com.github.davidmoten.odata.client;

import java.util.Map;

import com.github.davidmoten.odata.client.internal.ChangedFields;

public interface ODataEntity {

    Map<String, Object> getUnmappedFields();

    ChangedFields getChangedFields();
}
