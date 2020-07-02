package com.github.davidmoten.odata.client;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public interface HttpRequestOptions {

	/**
	 * If present returns the connect timeout for the request (after the SSL
	 * handshake).
	 * 
	 * @return request connect timeout in ms
	 */
	Optional<Long> requestConnectTimeoutMs();

	/**
	 * If present returns the read timeout for the request (after the SSL
	 * handshake).
	 * 
	 * @return request read timeout in ms
	 */
	Optional<Long> requestReadTimeoutMs();

	public static final HttpRequestOptions EMPTY = new HttpRequestOptionsImpl(Optional.empty(), Optional.empty());

	public static HttpRequestOptions create(Optional<Long> connectTimeoutMs, Optional<Long> readTimeoutMs) {
		return new HttpRequestOptionsImpl(connectTimeoutMs, readTimeoutMs);
	}

	public static Builder connectTimeout(long duration, TimeUnit unit) {
		return new Builder(unit.toMillis(duration));
	}

	public static HttpRequestOptions readTimeout(long duration, TimeUnit unit) {
		return create(Optional.empty(), Optional.of(unit.toMillis(duration)));
	}

	public static final class Builder {

		private final long connectTimeoutMs;

		Builder(long connectTimeoutMs) {
			this.connectTimeoutMs = connectTimeoutMs;
		}

		public HttpRequestOptions readTimeout(long duration, TimeUnit unit) {
			return create(Optional.of(connectTimeoutMs), Optional.of(unit.toMillis(duration)));
		}

	}
}
