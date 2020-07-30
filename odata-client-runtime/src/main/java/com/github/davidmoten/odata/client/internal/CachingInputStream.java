package com.github.davidmoten.odata.client.internal;

import java.io.IOException;
import java.io.InputStream;

public final class CachingInputStream extends InputStream {

	private final InputStream in;
	private final byte[] cache;
	private final byte[] singleByte = new byte[1];
	private int readPosition;
	private int writePosition;

	public CachingInputStream(InputStream in, int size) {
		this.in = in;
		this.cache = new byte[size];
	}

	@Override
	public int read() throws IOException {
		int v = read(singleByte, 0, 1);
		if (v == -1) {
			return v;
		} else {
			return singleByte[0];
		}
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (readPosition + len > cache.length) {
			throw new IllegalArgumentException("cannot read beyond size of input stream");
		}
		int readFromCache = Math.min(len, writePosition - readPosition);
		if (readFromCache > 0) {
			System.arraycopy(cache, readPosition, b, off, readFromCache);
			readPosition += readFromCache;
			off += readFromCache;
		}
		int readFromInputStream = len - readFromCache;
		if (readFromInputStream == 0) {
			return readFromCache;
		}
		int n = in.read(cache, writePosition, readFromInputStream);
		if (n == -1) {
			throw new IOException("end of stream encountered unexpectedly. Is the size field correct?");
		}
		System.arraycopy(cache, writePosition, b, off, n);
		writePosition += n;
		readPosition += n;
		return readFromCache + readFromInputStream;
	}

	public void reset() {
		readPosition = 0;
	}

}
