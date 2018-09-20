package com.github.davidmoten.odata.client;

public interface SchemaInfo {

    public Class<? extends ODataEntity> getEntityClassFromTypeWithNamespace(String name);

}
