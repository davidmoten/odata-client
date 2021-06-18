package com.github.davidmoten.odata.client.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.github.davidmoten.odata.client.RequestHeader;
import com.github.davidmoten.odata.client.RequestOptions;

public class RequestOptionsImpl implements RequestOptions {

    private final Optional<Long> requestConnectTimeoutMs;
    private final Optional<Long> requestReadTimeoutMs;
    private final List<RequestHeader> requestHeaders;
    private final Map<String, String> queries;
    private final Optional<String> urlOverride;

    public RequestOptionsImpl(Optional<Long> requestConnectTimeoutMs,
            Optional<Long> requestReadTimeoutMs, List<RequestHeader> requestHeaders,
            Map<String, String> queries, Optional<String> urlOverride) {
        this.requestConnectTimeoutMs = requestConnectTimeoutMs;
        this.requestReadTimeoutMs = requestReadTimeoutMs;
        this.requestHeaders = requestHeaders;
        this.queries = queries;
        this.urlOverride = urlOverride;

    }

    public RequestOptionsImpl(RequestOptions r) {
        this(r.requestConnectTimeoutMs(), r.requestReadTimeoutMs(), r.getRequestHeaders(),
                r.getQueries(), r.getUrlOverride());
    }

    public RequestOptionsImpl withConnectTimeoutMs(long duration, TimeUnit unit) {
        return new RequestOptionsImpl(Optional.of(unit.toMillis(duration)), requestReadTimeoutMs, requestHeaders,
                queries, urlOverride);
    }

    public RequestOptionsImpl withReadTimeoutMs(long duration, TimeUnit unit) {
        return new RequestOptionsImpl(requestConnectTimeoutMs, Optional.of(unit.toMillis(duration)), requestHeaders,
                queries, urlOverride);
    }

    public RequestOptionsImpl withRequestHeader(String name, String value) {
        List<RequestHeader> h = new ArrayList<>(requestHeaders);
        h.add(RequestHeader.create(name, value));
        return new RequestOptionsImpl(requestConnectTimeoutMs, requestReadTimeoutMs, h, queries,
                urlOverride);
    }

    @Override
    public Optional<Long> requestConnectTimeoutMs() {
        return requestConnectTimeoutMs;
    }

    @Override
    public Optional<Long> requestReadTimeoutMs() {
        return requestReadTimeoutMs;
    }

    @Override
    public List<RequestHeader> getRequestHeaders() {
        return requestHeaders;
    }

    @Override
    public Map<String, String> getQueries() {
        return queries;
    }

    @Override
    public Optional<String> getUrlOverride() {
        return urlOverride;
    }

}
