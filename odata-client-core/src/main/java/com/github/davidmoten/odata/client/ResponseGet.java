package com.github.davidmoten.odata.client;

import java.net.HttpURLConnection;

public class ResponseGet {

    private final int responseCode;
    private final String json;

    public ResponseGet(int responseCode, String json) {
        this.responseCode = responseCode;
        this.json = json;
    }

    public String getJson() {
        if (responseCode != HttpURLConnection.HTTP_OK) {
            return json;
        } else {
            throw new RuntimeException("responseCode=" + responseCode);
        }
    }

    public int getResponseCode() {
        return responseCode;
    }
}
