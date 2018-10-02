package com.github.davidmoten.odata.client.generator.model;

import org.oasisopen.odata.csdl.v4.TProperty;

import com.github.davidmoten.odata.client.generator.Names;

public final class Property {

    private final TProperty p;
    private final Names names;

    public Property(TProperty p, Names names) {
        this.p = p;
        this.names = names;
    }

}
