package com.github.davidmoten.odata.client.generator.model;

import org.oasisopen.odata.csdl.v4.TEntityKeyElement;

import com.github.davidmoten.odata.client.generator.Names;

public class KeyElement {

    private final TEntityKeyElement keyElement;
    private final Names names;

    public KeyElement(TEntityKeyElement keyElement, Names names) {
        this.keyElement = keyElement;
        this.names = names;
    }

}
