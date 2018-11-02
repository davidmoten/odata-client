package com.github.davidmoten.odata.client;

import com.github.davidmoten.odata.client.internal.ChangedFields;

public interface ODataEntityType extends ODataType {

    ChangedFields getChangedFields();
}
