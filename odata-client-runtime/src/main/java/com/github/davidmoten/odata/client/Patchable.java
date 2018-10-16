package com.github.davidmoten.odata.client;

public interface Patchable<T extends ODataEntity> {

    T patch();

    T put();
}
