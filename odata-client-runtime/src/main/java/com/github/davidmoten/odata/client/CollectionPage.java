package com.github.davidmoten.odata.client;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.odata.client.internal.RequestHelper;

/**
 * Note this object has been annotated with JsonProperty declarations but the
 * user should not try to unmarshall a CollectionPage from json because it will
 * be missing critical fields to operate correctly.
 *
 * @param <T> item type
 */
@JsonIgnoreType
@JsonPropertyOrder({"@odata.nextLink","@odata.deltaLink","value"})
public final class CollectionPage<T> implements Paged<T, CollectionPage<T>> {

    private final ContextPath contextPath;
    private final Class<T> cls;
    private final List<T> list;
    private final Optional<String> nextLink;
    private final Optional<String> deltaLink;
    private final SchemaInfo schemaInfo;
    private final List<RequestHeader> requestHeaders;
	private final HttpRequestOptions options;
	private final Consumer<? super CollectionPage<T>> listener;
	private final AtomicReference<CollectionPage<T>> latest = new AtomicReference<>();

    public CollectionPage(ContextPath contextPath, //
    		Class<T> cls, //
    		List<T> list, //
            Optional<String> nextLink, //
            Optional<String> deltaLink, //
            SchemaInfo schemaInfo, //
            List<RequestHeader> requestHeaders, //
            HttpRequestOptions options,
            Consumer<? super CollectionPage<T>> listener) {
		Preconditions.checkArgument(!nextLink.isPresent() || contextPath != null, "if nextLink is present contextPath must be non-null");
        Preconditions.checkNotNull(cls);
        Preconditions.checkNotNull(nextLink);
        Preconditions.checkNotNull(schemaInfo);
        Preconditions.checkNotNull(requestHeaders);
        Preconditions.checkNotNull(options);
        this.contextPath = contextPath;
        this.cls = cls;
        this.list = list == null ? Collections.emptyList() : list;
        this.nextLink = nextLink;
        this.deltaLink = deltaLink;
        this.schemaInfo = schemaInfo;
        this.requestHeaders = requestHeaders;
        this.options = options;
        if (listener == null) {
        	this.listener = latest::set;
        } else {
        	this.listener = listener;
        }
        latest.lazySet(this);
    }
    
    public CollectionPage(ContextPath contextPath, Class<T> cls, List<T> list,
            Optional<String> nextLink, SchemaInfo schemaInfo, List<RequestHeader> requestHeaders, HttpRequestOptions options) {
    	this(contextPath, cls, list, nextLink, Optional.empty(), schemaInfo, requestHeaders, options, null);
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
    
    @JsonInclude(Include.NON_NULL)
    @JsonProperty(value = "@odata.deltaLink")
    public Optional<String> deltaLink() {
    	return deltaLink;
    }
    
    /**
	 * Returns the next delta (if exists) as given by the last page of the
	 * current collection which may have a deltaLink attribute. If the last page
	 * hasn't been retrieved yet then all pages up to the last page will be
	 * retrieved first by this command.
	 * 
	 * @return the next delta collection if exists
	 */
	public Optional<CollectionPage<T>> nextDelta() {
		// ensure that all pages have been read of the current delta
		// because the last page has the delta link.
		CollectionPage<T> p = latest.get();
		while (p.nextLink().isPresent()) {
			p = p.nextPage().get();
		}
		return p.nextPage(p.deltaLink());
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
        return nextPage(nextLink);
    }

    Optional<CollectionPage<T>> nextPage(Optional<String> link) {
        if (link.isPresent()) {
            // TODO handle relative nextLink?
            HttpResponse response = contextPath.context().service().get(link.get(),
                    requestHeaders, options);
            // odata 4 says the "value" element of the returned json is an array of
            // serialized T see example at
            // https://www.odata.org/getting-started/basic-tutorial/#entitySet
            RequestHelper.checkResponseCode(contextPath, response, 200, 299);
            CollectionPage<T> page = contextPath.context().serializer().deserializeCollectionPage(
			        response.getText(), cls, contextPath, schemaInfo, requestHeaders, options, listener);
            listener.accept(page);
			return Optional.of(page);
        } else {
            return Optional.empty();
        }
    }

}
