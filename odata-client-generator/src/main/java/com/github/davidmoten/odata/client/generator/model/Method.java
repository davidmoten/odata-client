package com.github.davidmoten.odata.client.generator.model;

import java.util.Optional;

public interface Method {

    String getName();

    boolean isBoundToCollection();

    Optional<String> getBoundTypeWithNamespace();

    Optional<String> getBoundType();
}
