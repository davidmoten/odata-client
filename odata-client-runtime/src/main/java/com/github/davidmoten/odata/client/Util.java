package com.github.davidmoten.odata.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

public class Util {

    public static <T> T nvl(T object, T ifNull) {
        if (object == null) {
            return ifNull;
        } else {
            return object;
        }
    }
    
    static byte[] toByteArray(InputStream in) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int n;
        try {
            while ((n = in.read(buffer))!= -1) {
                bytes.write(buffer, 0, n);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return bytes.toByteArray();
    }
}
