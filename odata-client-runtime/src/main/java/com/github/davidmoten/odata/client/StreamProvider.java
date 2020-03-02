package com.github.davidmoten.odata.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.odata.client.internal.RequestHelper;

public final class StreamProvider {

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
        return RequestHelper.getStream(contextPath, options, base64);
    }

    public byte[] getBytes() {
        try (InputStream in = get()) {
            return Util.toByteArray(in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String getStringUtf8() {
        return new String(getBytes(), StandardCharsets.UTF_8);
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
