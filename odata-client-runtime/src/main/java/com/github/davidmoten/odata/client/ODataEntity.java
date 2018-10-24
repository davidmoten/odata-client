package com.github.davidmoten.odata.client;

import com.github.davidmoten.odata.client.internal.ChangedFields;

public interface ODataEntity extends HasUnmappedFields {

    ChangedFields getChangedFields();
}
