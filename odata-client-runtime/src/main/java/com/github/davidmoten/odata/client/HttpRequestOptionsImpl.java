package com.github.davidmoten.odata.client;

import java.util.Optional;

final class HttpRequestOptionsImpl implements HttpRequestOptions {

	private final Optional<Long> connectTimeoutMs;
	private final Optional<Long> readTimeoutMs;

	HttpRequestOptionsImpl(Optional<Long> connectTimeoutMs, Optional<Long> readTimeoutMs) {
		this.connectTimeoutMs = connectTimeoutMs;
		this.readTimeoutMs = readTimeoutMs;
	}

	@Override
	public Optional<Long> requestConnectTimeoutMs() {
		return connectTimeoutMs;
	}

	@Override
	public Optional<Long> requestReadTimeoutMs() {
		return readTimeoutMs;
	}

}
