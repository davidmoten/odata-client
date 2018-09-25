package com.github.davidmoten.odata.client;

import java.net.HttpURLConnection;

public class HttpResponse {

    private final int responseCode;
    private final String text;

    public HttpResponse(int responseCode, String text) {
        this.responseCode = responseCode;
        this.text = text;
    }

    public String getText() {
        return getText(HttpURLConnection.HTTP_OK);
    }

    public String getText(int expectedResponseCode) {
        if (responseCode == expectedResponseCode) {
            return text;
        } else {
            throw new RuntimeException("responseCode=" + responseCode);
        }
    }

    public int getResponseCode() {
        return responseCode;
    }
}
