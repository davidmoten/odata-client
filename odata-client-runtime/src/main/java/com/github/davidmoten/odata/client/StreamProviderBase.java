package com.github.davidmoten.odata.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

public interface StreamProviderBase {

    /**
     * Returns a {@link InputStream} for the requested content every time this
     * method is called. Note that the returned `InputStream` <b>must</b> be closed
     * after read otherwise the http client may hang on a later request. Use the
     * {@link #contentType()} method to get the mime type of the content delivered
     * via the InputStream.
     *
     * @return InputStream for the requested content that must be closed after use
     */
    InputStream get();

    default byte[] getBytes() {
        try (InputStream in = get()) {
            return Util.toByteArray(in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    default String getStringUtf8() {
        return new String(getBytes(), StandardCharsets.UTF_8);
    }

}
