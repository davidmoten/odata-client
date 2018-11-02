package com.github.davidmoten.odata.client;

public interface Patchable<T extends ODataEntityType> {

    T patch();

    T put();
}
