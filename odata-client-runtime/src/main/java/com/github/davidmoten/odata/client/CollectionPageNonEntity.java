package com.github.davidmoten.odata.client;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

@JsonIgnoreType
public final class CollectionPageNonEntity<T> implements Paged<T, CollectionPageNonEntity<T>> {

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

    @Override
    public List<T> currentPage() {
        return list;
    }

    @Override
    public Optional<CollectionPageNonEntity<T>> nextPage() {
        if (nextLink != null) {
            // TODO
            throw new UnsupportedOperationException();
        } else {
            return Optional.empty();
        }
    }

}
