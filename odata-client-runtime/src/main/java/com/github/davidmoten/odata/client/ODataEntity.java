package com.github.davidmoten.odata.client;

import com.github.davidmoten.odata.client.internal.ChangedFields;

public interface ODataEntity extends ODataType {

    ChangedFields getChangedFields();
}
