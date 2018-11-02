package com.github.davidmoten.odata.client;

public interface SchemaInfo {

    public Class<? extends ODataType> getClassFromTypeWithNamespace(String name);

}
