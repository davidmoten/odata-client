package com.github.davidmoten.odata.client;

public class HttpResponse {

    private final int responseCode;
    private final String text;

    public HttpResponse(int responseCode, String text) {
        this.responseCode = responseCode;
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public int getResponseCode() {
        return responseCode;
    }
}
