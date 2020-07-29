package com.github.davidmoten.odata.client;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.odata.client.internal.RequestHelper;

public final class StreamUploaderChunked implements StreamUploader<StreamUploaderChunked> {

    private static final Logger log = LoggerFactory.getLogger(StreamUploaderChunked.class);

    // TODO MsGraph restriction only? Make configurable via property?
    private static final int BASE_BYTE_RANGE_SIZE = 327680;

    private final ContextPath contextPath;
    private final List<RequestHeader> requestHeaders;
	private Optional<Long> connectTimeoutMs = Optional.empty();
	private Optional<Long> readTimeoutMs = Optional.empty();

    StreamUploaderChunked(ContextPath contextPath, String contentType) {
        this.contextPath = contextPath;
        this.requestHeaders = new ArrayList<>();
        requestHeaders.add(RequestHeader.contentType(contentType));
    }
    
    public StreamUploaderChunked requestHeader(String name, String value) {
        requestHeaders.add(RequestHeader.create(name, value));
        return this;
    }

    public StreamUploaderChunked connectTimeout(long duration, TimeUnit unit) {
    	this.connectTimeoutMs = Optional.of(unit.toMillis(duration));
    	return this;
    }
    
    public StreamUploaderChunked readTimeout(long duration, TimeUnit unit) {
    	this.readTimeoutMs = Optional.of(unit.toMillis(duration));
    	return this;
    }
    
    public void upload(InputStream in, long size, int chunkSize) {
        upload(in, size, chunkSize, Retries.NONE);
    }

    public void upload(InputStream in, long size, int chunkSize, Retries retries) {
        // see
        // https://docs.microsoft.com/en-us/graph/api/driveitem-createuploadsession?view=graph-rest-1.0
        Preconditions.checkArgument(chunkSize >= 0);
        int rem = chunkSize % BASE_BYTE_RANGE_SIZE;
        if (rem > 0 || chunkSize == 0) {
            chunkSize += chunkSize - rem;
        }
        HttpRequestOptions options = HttpRequestOptions.create(connectTimeoutMs, readTimeoutMs);
        //TODO do we use edit url?
        String uploadUrl = contextPath.toUrl();

        // get the post url and then send each chunk to the post url
        // without Authorization header

        byte[] buffer = new byte[chunkSize];
        for (int i = 0; i < size; i += chunkSize) {
            int start = i;
            final int chunk;
            try {
                chunk = in.read(buffer, 0, chunkSize);
            } catch (IOException e) {
                // this is unrecoverable because we cannot reread from the InputStream
                // TODO provide a Supplier<InputStream> parameter?
                throw new UncheckedIOException(e);
            }
            retries.performWithRetries(() -> {
                    log.debug("putting chunk " + start + ", size=" + chunk);
                    RequestHelper.putChunk(contextPath.context().service(), uploadUrl, new ByteArrayInputStream(buffer, 0, chunk), requestHeaders,
                            start, Math.min(size, start + chunk), size, options);
            });
        }
    }

    public void upload(byte[] bytes, int chunkSize) {
        upload(bytes, chunkSize, Retries.NONE);
    }
    
    public void upload(File file, int chunkSize, Retries retries) {
        try (InputStream in = new FileInputStream(file)) {
            upload(in, (int) file.length(), chunkSize, retries);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    public void upload(File file, int chunkSize) {
        upload(file, chunkSize, Retries.NONE);
    }
    
    public void uploadUtf8(String text, int chunkSize) {
        upload(text.getBytes(StandardCharsets.UTF_8), chunkSize);
    }
    
    public void upload(byte[] bytes, int chunkSize, Retries retries) {
        upload(new ByteArrayInputStream(bytes), bytes.length, chunkSize, retries);
    }
}
