package com.github.davidmoten.odata.client;

import java.util.List;
import java.util.Optional;

public final class CollectionPageNonEntity<T> {

    private final ContextPath contextPath;
    private final Class<T> cls;
    private final List<T> list;
    private final String nextLink;

    public CollectionPageNonEntity(ContextPath contextPath, Class<T> cls, List<T> list, String nextLink) {
        this.contextPath = contextPath;
        this.cls = cls;
        this.list = list;
        this.nextLink = nextLink;
    }

    public List<T> values() {
        return list;
    }

    public Optional<CollectionPageNonEntity<T>> nextPage() {
        if (nextLink != null) {
            // TODO
            return null;
        } else {
            return Optional.empty();
        }
    }

}
