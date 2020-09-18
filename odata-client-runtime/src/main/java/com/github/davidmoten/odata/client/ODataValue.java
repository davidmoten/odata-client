package com.github.davidmoten.odata.client;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.davidmoten.odata.client.internal.UnmappedFieldsImpl;

public class ODataValue<T> {

    @JacksonInject
    @JsonIgnore
    private UnmappedFieldsImpl unmappedFields;

    @JsonProperty("value")
    private T value;

    public UnmappedFields unmappedFields() {
        return unmappedFields;
    }

    public T value() {
        return value;
    }

}
