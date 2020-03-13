package com.github.davidmoten.odata.client;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.odata.client.internal.RequestHelper;

public final class StreamUploader {

    private static final int BASE_BYTE_RANGE_SIZE = 327680;

    private final ContextPath contextPath;
    private final Map<String, String> queries;
    private final List<RequestHeader> requestHeaders;

    public StreamUploader(ContextPath contextPath, String contentType) {
        this.contextPath = contextPath;
        this.queries = new HashMap<>();
        this.requestHeaders = new ArrayList<>();
        requestHeaders.add(RequestHeader.create("Content-Type", contentType));
    }

    public StreamUploader requestHeader(String name, String value) {
        requestHeaders.add(RequestHeader.create(name, value));
        return this;
    }

    public void upload(InputStream in) {
        RequestHelper.put(contextPath, RequestOptions.create(queries, requestHeaders), in);
    }

    public void upload(InputStream in, UploadListener listener, int reportingChunkSize) {
        upload(new InputStreamWithProgress(in, reportingChunkSize, listener));
    }

    public void upload(InputStream in, UploadListener listener) {
        upload(in, listener, 8192);
    }

    public void upload(InputStream in, long size) {
        // approx 10MB chunks
        upload(in, size, 30 * BASE_BYTE_RANGE_SIZE);
    }

    public void upload(InputStream in, long size, int chunkSize) {
        // see
        // https://docs.microsoft.com/en-us/graph/api/driveitem-createuploadsession?view=graph-rest-1.0
        Preconditions.checkArgument(chunkSize >= 0);
        int rem = chunkSize % BASE_BYTE_RANGE_SIZE;
        if (rem > 0 || chunkSize == 0) {
            chunkSize += chunkSize - rem;
        }
        String uploadUrl = RequestHelper.createUploadSession(contextPath, requestHeaders);

        // get the post url and then send each chunk to the post url
        // without Authorization header
        for (int i = 0; i < size; i += chunkSize) {
            RequestHelper.putChunk(uploadUrl, in, requestHeaders, i, Math.min(size, i + chunkSize),
                    size);
        }
    }

}
