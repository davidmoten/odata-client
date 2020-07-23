package com.github.davidmoten.odata.client;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.odata.client.internal.RequestHelper;

public final class StreamUploaderChunked {

    // TODO MsGraph restriction only? Make configurable via property?
    private static final int BASE_BYTE_RANGE_SIZE = 327680;

    private final ContextPath contextPath;
    private final String fieldName;
    private final String contentType;
    private final List<RequestHeader> requestHeaders;
	private Optional<Long> connectTimeoutMs = Optional.empty();
	private Optional<Long> readTimeoutMs = Optional.empty();

    StreamUploaderChunked(ContextPath contextPath, String fieldName,
            String contentType) {
        this.contextPath = contextPath;
        this.fieldName = fieldName;
        this.contentType = contentType;
        this.requestHeaders = new ArrayList<>();
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
        String uploadUrl = RequestHelper.createUploadSession(contextPath.addSegment(fieldName),
                requestHeaders, contentType, options);

        // get the post url and then send each chunk to the post url
        // without Authorization header

        for (int i = 0; i < size; i += chunkSize) {
            int attempt = 0;
            Throwable error = null;
            Function<? super Throwable, Boolean> keepGoingIf = retries.keepGoingIf().get();
            Iterator<Long> intervalsMs = retries.retryIntervalsMs().iterator();
            while (true) {
                if (attempt > retries.maxRetries()) {
                    if (error != null) {
                        throw new RetryException("attempts greater than maxRetries", error);
                    } else {
                        throw new RetryException("attempts greater than maxRetries");
                    }
                }
                try {
                    RequestHelper.putChunk(contextPath.context().service(), uploadUrl, in, requestHeaders,
                            i, Math.min(size, i + chunkSize), size, options);
                    break;
                } catch (Throwable e) {
                    error = e;
                    if (!keepGoingIf.apply(e)) {
                        throw new RetryException("exception not retryable", e);
                    }
                    if (!intervalsMs.hasNext()) {
                        throw new RetryException("stopping retries because no more intervals specified");
                    }
                    try {
                        Thread.sleep(intervalsMs.next());
                    } catch (InterruptedException interruptedException) {
                        throw new RetryException("interrupted", interruptedException);
                    }
                }
                attempt++;
            }
        }
    }
}
