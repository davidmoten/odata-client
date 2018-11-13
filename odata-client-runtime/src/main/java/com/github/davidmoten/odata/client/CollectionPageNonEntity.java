package com.github.davidmoten.odata.client;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

@JsonIgnoreType
public final class CollectionPageNonEntity<T> implements Paged<T, CollectionPageNonEntity<T>> {

    private final ContextPath contextPath;
    private final Class<T> cls;
    private final List<T> list;
    private final Optional<String> nextLink;

    public CollectionPageNonEntity(ContextPath contextPath, Class<T> cls, List<T> list, Optional<String> nextLink) {
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
        // if (nextLink.isPresent()) {
        // // TODO add request headers used in initial call?
        // // TODO handle relative nextLink?
        // HttpResponse response = contextPath.context().service().get(nextLink.get(),
        // Collections.emptyList());
        // // odata 4 says the "value" element of the returned json is an array of
        // // serialized T see example at
        // // https://www.odata.org/getting-started/basic-tutorial/#entitySet
        // return
        // Optional.of(contextPath.context().serializer().deserializeCollectionPageEntity(response.getText(),
        // cls, contextPath, schemaInfo));
        // } else {
        // return Optional.empty();
        // }
    }

}
