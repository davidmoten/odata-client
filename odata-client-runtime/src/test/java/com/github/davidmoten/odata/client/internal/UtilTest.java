package com.github.davidmoten.odata.client.internal;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import com.github.davidmoten.odata.client.ODataType;
import com.github.davidmoten.odata.client.UnmappedFields;

public class UtilTest {

    @Test
    public void testODataTypeNameFromAnyString() {
        assertEquals("Edm.String", Util.odataTypeNameFromAny(String.class));
    }

    @Test
    public void testODataTypeNameFromAnyODataTypeTemp() {
        assertEquals("hello.there", Util.odataTypeNameFromAny(Temp.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testODataTypeNameFromAnyObject() {
        Util.odataTypeNameFromAny(Object.class);
    }

    @Test
    public void testReadFully() throws IOException {
        try (InputStream in = createInputStream(6)) {
            byte[] b = new byte[6];
            assertEquals(6, Util.readFully(in, b, 6));
            assertArrayEquals(new byte[] { 1, 2, 3, 4, 5, 6 }, b);
        }
    }

    @Test
    public void testReadFullyPastEndOfStream() throws IOException {
        try (InputStream in = createInputStream(6)) {
            byte[] b = new byte[6];
            assertEquals(6, Util.readFully(in, b, 8));
            assertArrayEquals(new byte[] { 1, 2, 3, 4, 5, 6 }, b);
        }
    }

    @Test
    public void testReadFullyEmptyStream() throws IOException {
        try (InputStream in = createInputStream(0)) {
            byte[] b = new byte[6];
            assertEquals(0, Util.readFully(in, b, 8));
            assertArrayEquals(new byte[] { 0, 0, 0, 0, 0, 0 }, b);
        }
    }

    private InputStream createInputStream(int maxLength) {
        InputStream in = new InputStream() {
            int i = 0;

            @Override
            public int read() throws IOException {
                if (i == maxLength) {
                    return -1;
                } else {
                    return (i++ + 1) & 0xFF;
                }
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                int v = read();
                if (v == -1) {
                    return -1;
                } else {
                    b[off] = (byte) v;
                    return 1;
                }
            }
        };
        return in;
    }

    public final static class Temp implements ODataType {

        protected Temp() {

        }

        @Override
        public String odataTypeName() {
            return "hello.there";
        }

        @Override
        public UnmappedFields getUnmappedFields() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void postInject(boolean addKeysToContextPath) {
            // TODO Auto-generated method stub

        }

    }

}
