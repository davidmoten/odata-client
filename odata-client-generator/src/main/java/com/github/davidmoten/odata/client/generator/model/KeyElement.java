package com.github.davidmoten.odata.client.generator.model;

import java.util.List;
import java.util.stream.Collectors;

import org.oasisopen.odata.csdl.v4.TEntityKeyElement;

import com.github.davidmoten.odata.client.generator.Names;

public class KeyElement {

    private final TEntityKeyElement keyElement;
    private final EntityType entityType;
    private final Names names;

    public KeyElement(TEntityKeyElement keyElement, EntityType entityType, Names names) {
        this.keyElement = keyElement;
        this.entityType = entityType;
        this.names = names;
    }

    public List<PropertyRef> getPropertyRefs() {
        return keyElement.getPropertyRef().stream().map(x -> new PropertyRef(x, entityType, names))
                .collect(Collectors.toList());
    }

}
