package com.github.davidmoten.odata.client;

public interface NonEntityRequestFactory<T extends ODataType, R extends NonEntityRequest<T>> {

    R create(ContextPath contextPath);

}
