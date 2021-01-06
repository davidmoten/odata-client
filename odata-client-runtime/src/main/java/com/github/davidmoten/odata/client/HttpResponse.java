package com.github.davidmoten.odata.client;

import java.nio.charset.StandardCharsets;

public class HttpResponse {

    private final int responseCode;
    private final byte[] bytes;

    public HttpResponse(int responseCode, byte[] bytes) {
        this.responseCode = responseCode;
        this.bytes = bytes;
    }

    public String getText() {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public int getResponseCode() {
        return responseCode;
    }

    public byte[] getBytes() {
        return bytes;
    }
}
