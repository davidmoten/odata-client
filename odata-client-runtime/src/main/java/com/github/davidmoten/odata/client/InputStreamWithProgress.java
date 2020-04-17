package com.github.davidmoten.odata.client;

import java.io.IOException;
import java.io.InputStream;

public final class InputStreamWithProgress extends InputStream {

    private final InputStream in;
    private final int chunkSize;
    private long bytesRead;
    private int bytesReadInChunk;
    private final UploadListener listener;

    public InputStreamWithProgress(InputStream in, int chunkSize, UploadListener listener) {
        this.in = in;
        this.chunkSize = chunkSize;
        this.listener = listener;
    }

    @Override
    public int read() throws IOException {
        if (bytesRead > 0 && bytesReadInChunk >= chunkSize) {
            listener.bytesWritten(bytesRead);
        }
        int v = in.read();
        if (v != -1) {
            bytesRead++;
            bytesReadInChunk++;
        }
        return v;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (bytesRead > 0 && bytesReadInChunk >= chunkSize) {
            listener.bytesWritten(bytesRead);
        }
        int count = in.read(b, off, len);
        if (count != -1) {
            bytesRead += count;
            bytesReadInChunk += count;
        }
        return count;
    }

}
