package com.github.davidmoten.odata.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.odata.client.internal.TypedObject;

abstract class ActionFunctionRequestBase<T extends ActionFunctionRequestBase<T>> {

	protected final ContextPath contextPath;
	protected final Map<String, TypedObject> parameters;
	protected final List<RequestHeader> requestHeaders = new ArrayList<>();
	protected final Map<String, String> queries = new HashMap<>();
	private Optional<Long> connectTimeoutMs = Optional.empty();
	private Optional<Long> readTimeoutMs = Optional.empty();

	ActionFunctionRequestBase(Map<String, TypedObject> parameters, ContextPath contextPath) {
		this.parameters = parameters;
		this.contextPath = contextPath;
	}

	@SuppressWarnings("unchecked")
	public T requestHeader(RequestHeader requestHeader) {
		requestHeaders.add(requestHeader);
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T query(String key, String value) {
		Preconditions.checkNotNull(key);
		Preconditions.checkNotNull(value);
		queries.put(key, value);
		return (T) this;
	}

	public T select(String clause) {
		return query("$select", clause);
	}

	public T expand(String clause) {
		return query("$expand", clause);
	}

	public T filter(String clause) {
		return query("$filter", clause);
	}

	public T search(String clause) {
		return query("$search", clause);
	}

	public T orderBy(String clause) {
		return query("$orderBy", clause);
	}

	public T skip(long skip) {
		return query("$skip", String.valueOf(skip));
	}

	public T top(long top) {
		return query("$top", String.valueOf(top));
	}

	public T connectTimeout(long duration, TimeUnit unit) {
		this.connectTimeoutMs = Optional.of(unit.toMillis(duration));
		return (T) this;
	}

	public T readTimeout(long duration, TimeUnit unit) {
		this.readTimeoutMs = Optional.of(unit.toMillis(duration));
		return (T) this;
	}

	public T useCaches() {
		// TODO support useCaches
		throw new UnsupportedOperationException();
	}

	public T metadataNone() {
		return requestHeader(RequestHeader.acceptJsonWithMetadata("none"));
	}

	public T metadataMinimal() {
		return requestHeader(RequestHeader.acceptJsonWithMetadata("minimal"));
	}

	public T metadataFull() {
		return requestHeader(RequestHeader.acceptJsonWithMetadata("full"));
	}

	protected RequestOptions options() {
		return new RequestOptions() {
			
			@Override
			public List<RequestHeader> getRequestHeaders() {
				return requestHeaders;
			}

			@Override
			public Map<String, String> getQueries() {
				return queries;
			}

			@Override
			public Optional<Long> requestConnectTimeoutMs() {
				return connectTimeoutMs;
			}

			@Override
			public Optional<Long> requestReadTimeoutMs() {
				return readTimeoutMs;
			}

			@Override
			public Optional<String> getUrlOverride() {
				return Optional.empty();
			}
		};
	}

}