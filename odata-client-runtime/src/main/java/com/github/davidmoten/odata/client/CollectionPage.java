package com.github.davidmoten.odata.client;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

//TODO merge CollectionPageNonEntity and CollectionPageEntity into CollectionPage
@JsonIgnoreType
public final class CollectionPage<T> implements Paged<T, CollectionPage<T>> {

    private final ContextPath contextPath;
    private final Class<T> cls;
    private final List<T> list;
    private final Optional<String> nextLink;
    private final SchemaInfo schemaInfo;
    private final List<RequestHeader> requestHeaders;

    public CollectionPage(ContextPath contextPath, Class<T> cls, List<T> list,
            Optional<String> nextLink, SchemaInfo schemaInfo, List<RequestHeader> requestHeaders) {
        this.contextPath = contextPath;
        this.cls = cls;
        this.list = list;
        this.nextLink = nextLink;
        this.schemaInfo = schemaInfo;
        this.requestHeaders = requestHeaders;
    }

    @Override
    public List<T> currentPage() {
        return list;
    }

    @Override
    public Optional<CollectionPage<T>> nextPage() {
        if (nextLink.isPresent()) {
            // TODO handle relative nextLink?
            HttpResponse response = contextPath.context().service().get(nextLink.get(),
                    requestHeaders);
            // odata 4 says the "value" element of the returned json is an array of
            // serialized T see example at
            // https://www.odata.org/getting-started/basic-tutorial/#entitySet
            return Optional
                    .of(contextPath.context().serializer().deserializeCollectionPageNonEntity(
                            response.getText(), cls, contextPath, schemaInfo, requestHeaders));
        } else {
            return Optional.empty();
        }
    }

}
