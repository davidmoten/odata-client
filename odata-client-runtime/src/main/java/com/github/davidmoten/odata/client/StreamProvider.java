package com.github.davidmoten.odata.client;

import java.io.InputStream;
import java.util.function.Supplier;

import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.odata.client.internal.RequestHelper;

public final class StreamProvider implements Supplier<InputStream> {

    private final ContextPath contextPath;
    private final RequestOptions options;
    private final String contentType;

    public StreamProvider(ContextPath contextPath, RequestOptions options, String contentType) {
        Preconditions.checkNotNull(contextPath);
        Preconditions.checkNotNull(contentType);
        this.contextPath = contextPath;
        this.options = options;
        this.contentType = contentType;
    }

    @Override
    public InputStream get() {
        return RequestHelper.getStream(contextPath, options);
    }

    public String contentType() {
        return contentType;
    }

}
