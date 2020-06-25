package com.github.davidmoten.odata.client;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.github.davidmoten.odata.client.internal.RequestHelper;

/**
 * Note this object has been annotated with JsonProperty declarations but the
 * user should not try to unmarshall a CollectionPage from json because it will
 * be missing critical fields to operate correctly.
 *
 * @param <T> item type
 */
@JsonIgnoreType
@JsonPropertyOrder({"@odata.nextLink","value"})
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
    @JsonProperty(value = "value")
    public List<T> currentPage() {
        return list;
    }
    
    @JsonProperty(value = "@odata.nextLink")
    public Optional<String> nextLink() {
        return nextLink;
    }
    
    /**
     * Returns the list of items in odata collection formatted json but with one
     * optionally present {@code @odata.nextLink} entry. The list of items is
     * represented by an array with field name {@code value}.
     * 
     * @return json for the list plus nextLink
     */
    public String toJsonMinimal() {
        return Serializer.INSTANCE.serialize(this);
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
            RequestHelper.checkResponseCode(contextPath, response, 200, 299);
            return Optional
                    .of(contextPath.context().serializer().deserializeCollectionPage(
                            response.getText(), cls, contextPath, schemaInfo, requestHeaders));
        } else {
            return Optional.empty();
        }
    }

}
