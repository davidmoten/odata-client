package com.github.davidmoten.msgraph;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

class Util {
    static byte[] read(InputStream in) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int n;
        while ((n = in.read(buffer)) != -1) {
            bytes.write(buffer, 0, n);
        }
        return bytes.toByteArray();
    }
}
