package com.github.davidmoten.odata.client.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public final class Util {

    public static String readString(InputStream in, Charset charset) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = in.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return new String(result.toByteArray(), charset);
    }

    public static String trimTrailingSlash(String url) {
        if (!url.isEmpty() && url.charAt(url.length() - 1) == '/') {
            return url.substring(0, url.length() - 2);
        } else {
            return url;
        }
    }

}
