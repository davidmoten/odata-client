package com.github.davidmoten.odata.client;

import java.io.InputStream;

import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.odata.client.internal.RequestHelper;

public final class StreamProvider {

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

    /**
     * Returns a {@link InputStream} for the requested content every time this
     * method is called. Note that the returned `InputStream` <b>must</b> be closed
     * after read otherwise the http client may hang on a later request. Use the
     * {@link #contentType()} method to get the mime type of the content delivered
     * via the InputStream.
     *
     * @return InputStream for the requested content that must be closed after use
     */
    public InputStream get() {
        return RequestHelper.getStream(contextPath, options);
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
