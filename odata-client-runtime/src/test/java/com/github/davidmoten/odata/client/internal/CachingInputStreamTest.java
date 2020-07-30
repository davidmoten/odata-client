package com.github.davidmoten.odata.client.internal;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class CachingInputStreamTest {

	@Test
	public void testReadAllBytes() throws IOException {
		InputStream in = new ByteArrayInputStream(new byte[] { 1, 2, 3, 4, 5 });
		try (InputStream cin = new CachingInputStream(in, 5)) {
			assertEquals(1, cin.read());
			assertEquals(2, cin.read());
			assertEquals(3, cin.read());
			assertEquals(4, cin.read());
			assertEquals(5, cin.read());
		}
	}

	@Test
	public void testReadAllBytesIntoArray() throws IOException {
		InputStream in = new ByteArrayInputStream(new byte[] { 1, 2, 3, 4, 5 });
		try (InputStream cin = new CachingInputStream(in, 5)) {
			byte[] bytes = new byte[8];
			cin.read(bytes, 1, 5);
			assertEquals(1, bytes[1]);
			assertEquals(2, bytes[2]);
			assertEquals(3, bytes[3]);
			assertEquals(4, bytes[4]);
			assertEquals(5, bytes[5]);
		}
	}

	@Test
	public void testReadAllBytesInTwoReads() throws IOException {
		InputStream in = new ByteArrayInputStream(new byte[] { 1, 2, 3, 4, 5 });
		try (InputStream cin = new CachingInputStream(in, 5)) {
			byte[] bytes = new byte[8];
			cin.read(bytes, 1, 2);
			assertEquals(1, bytes[1]);
			assertEquals(2, bytes[2]);
			assertEquals(0, bytes[3]);

			cin.read(bytes, 3, 3);
			assertEquals(1, bytes[1]);
			assertEquals(2, bytes[2]);
			assertEquals(3, bytes[3]);
			assertEquals(4, bytes[4]);
			assertEquals(5, bytes[5]);
			assertEquals(0, bytes[6]);
		}
	}

	@Test
	public void testReset() throws IOException {
		InputStream in = new ByteArrayInputStream(new byte[] { 1, 2, 3, 4, 5 });
		try (InputStream cin = new CachingInputStream(in, 5)) {
			assertEquals(1, cin.read());
			assertEquals(2, cin.read());
			cin.reset();
			byte[] bytes = new byte[8];
			assertEquals(5, cin.read(bytes, 1, 5));
			assertEquals(1, bytes[1]);
			assertEquals(2, bytes[2]);
			assertEquals(3, bytes[3]);
			assertEquals(4, bytes[4]);
			assertEquals(5, bytes[5]);
		}
	}

	@Test(expected=IllegalArgumentException.class)
	public void testReadBeyondSize() throws IOException {
		InputStream in = new ByteArrayInputStream(new byte[] { 1, 2, 3, 4, 5 });
		try (InputStream cin = new CachingInputStream(in, 5)) {
			byte[] bytes = new byte[6];
			cin.read(bytes, 0, 6);
		}
	}

}
