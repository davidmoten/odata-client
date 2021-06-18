package com.github.davidmoten.odata.client;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.odata.client.internal.RequestHelper;
import com.github.davidmoten.odata.client.internal.RequestOptionsImpl;

public final class StreamProvider implements StreamProviderBase {

    private final ContextPath contextPath;
    private final RequestOptions options;
    private final String contentType;
    private final String base64;

    public StreamProvider(ContextPath contextPath, RequestOptions options, String contentType,
            String base64) {
        Preconditions.checkNotNull(contextPath);
        Preconditions.checkNotNull(contentType);
        this.contextPath = contextPath;
        this.options = options;
        this.contentType = contentType;
        this.base64 = base64;
    }
    
    public StreamProvider requestHeader(String name, String value) {
        RequestOptionsImpl o = new RequestOptionsImpl(options).withRequestHeader(name, value);
        return new StreamProvider(contextPath, o, contentType, base64);
    }
    
    public StreamProvider connectTimeout(long duration, TimeUnit unit) {
        Preconditions.checkNotNull(unit);
        RequestOptionsImpl o = new RequestOptionsImpl(options).withConnectTimeoutMs(duration, unit);
        return new StreamProvider(contextPath, o, contentType, base64);
    }
    
    public StreamProvider readTimeout(long duration, TimeUnit unit) {
        Preconditions.checkNotNull(unit);
        RequestOptionsImpl o = new RequestOptionsImpl(options).withReadTimeoutMs(duration, unit);
        return new StreamProvider(contextPath, o, contentType, base64);
    }
    
    public InputStream get() {
        return RequestHelper.getStream(contextPath, options, base64);
    }

    /**
     * Returns the HTTP <i>ContentType</i> for the content delivered by
     * {@link #get()} method.
     *
     * @return HTTP ContentType value
     */
    public String contentType() {
        return contentType;
    }

}
