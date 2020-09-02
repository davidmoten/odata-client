package com.github.davidmoten.odata.client;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.davidmoten.guavamini.Preconditions;

public final class ObjectOrDeltaLink<T> {

    @JsonProperty("object")
    private final Optional<T> object;

    @JsonProperty("deltaLink")
    private final Optional<String> deltaLink;

    ObjectOrDeltaLink(Optional<T> object, Optional<String> deltaLink) {
        Preconditions.checkNotNull(object);
        Preconditions.checkNotNull(deltaLink);
        Preconditions.checkArgument(object.isPresent() && !deltaLink.isPresent() || !object.isPresent());
        this.object = object;
        this.deltaLink = deltaLink;
    }

    public Optional<T> object() {
        return object;
    }

    public Optional<String> deltaLink() {
        return deltaLink;
    }

}