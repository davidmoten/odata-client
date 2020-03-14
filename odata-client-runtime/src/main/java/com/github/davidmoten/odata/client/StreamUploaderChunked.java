package com.github.davidmoten.odata.client;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.odata.client.internal.RequestHelper;

public final class StreamUploaderChunked {

    // TODO MsGraph restriction only? Make configurable via property?
    private static final int BASE_BYTE_RANGE_SIZE = 327680;

    private final ContextPath contextPath;
    private final ODataType entity;
    private final String fieldName;
    private final String contentType;
    private final List<RequestHeader> requestHeaders;

    StreamUploaderChunked(ContextPath contextPath, ODataType entity, String fieldName,
            String contentType) {
        this.contextPath = contextPath;
        this.entity = entity;
        this.fieldName = fieldName;
        this.contentType = contentType;
        this.requestHeaders = new ArrayList<>();
    }

    public void upload(InputStream in, long size, int chunkSize) {
        // see
        // https://docs.microsoft.com/en-us/graph/api/driveitem-createuploadsession?view=graph-rest-1.0
        Preconditions.checkArgument(chunkSize >= 0);
        int rem = chunkSize % BASE_BYTE_RANGE_SIZE;
        if (rem > 0 || chunkSize == 0) {
            chunkSize += chunkSize - rem;
        }
        String uploadUrl = RequestHelper.createUploadSession(contextPath.addSegment(fieldName),
                requestHeaders, contentType);

        // get the post url and then send each chunk to the post url
        // without Authorization header
        for (int i = 0; i < size; i += chunkSize) {
            RequestHelper.putChunk(contextPath.context().service(), uploadUrl, in, requestHeaders,
                    i, Math.min(size, i + chunkSize), size);
        }
    }

}
