package com.github.davidmoten.odata.client.internal;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

final class InputStreamWithCloseable extends InputStream {

    private final InputStream in;
    private final Closeable closeable;

    InputStreamWithCloseable(InputStream in, Closeable closeable) {
        this.in = in;
        this.closeable = closeable;
    }

    @Override
    public int read() throws IOException {
        return in.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return in.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return in.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return in.skip(n);
    }

    @Override
    public int available() throws IOException {
        return in.available();
    }

    @Override
    public void close() throws IOException {
        try {
            in.close();
        } finally {
            closeable.close();
        }
    }

    @Override
    public synchronized void mark(int readlimit) {
        in.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        in.reset();
    }

    @Override
    public boolean markSupported() {
        return in.markSupported();
    }

}
