package com.github.davidmoten.odata.client;

import java.net.HttpURLConnection;

public class ResponseGet {

    private final int responseCode;
    private final String text;

    public ResponseGet(int responseCode, String text) {
        this.responseCode = responseCode;
        this.text = text;
    }

    public String getText() {
        if (responseCode != HttpURLConnection.HTTP_OK) {
            return text;
        } else {
            throw new RuntimeException("responseCode=" + responseCode);
        }
    }

    public int getResponseCode() {
        return responseCode;
    }
}
