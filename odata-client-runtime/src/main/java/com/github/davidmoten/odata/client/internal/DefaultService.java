package com.github.davidmoten.odata.client.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;

import com.github.davidmoten.odata.client.ClientException;
import com.github.davidmoten.odata.client.HttpMethod;
import com.github.davidmoten.odata.client.HttpResponse;
import com.github.davidmoten.odata.client.Path;
import com.github.davidmoten.odata.client.Service;

public final class DefaultService implements Service {

    private final Path basePath;

    public DefaultService(Path basePath) {
        this.basePath = basePath;
    }

    @Override
    public HttpResponse GET(String url, Map<String, String> requestHeaders) {
        try {
            URL u = new URL(url);
            HttpURLConnection c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod(HttpMethod.GET.toString());
            for (Entry<String, String> entry : requestHeaders.entrySet()) {
                c.setRequestProperty(entry.getKey(), entry.getValue());
            }
            c.setDoOutput(false);
            c.setDoInput(true);
            String text = Util.readString(c.getInputStream(), StandardCharsets.UTF_8);
            return new HttpResponse(c.getResponseCode(), text);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public HttpResponse PATCH(String url, Map<String, String> requestHeaders, String content) {
        return patch(url, requestHeaders, content, HttpMethod.PATCH);
    }

    @Override
    public HttpResponse PUT(String url, Map<String, String> requestHeaders, String content) {
        return patch(url, requestHeaders, content, HttpMethod.PUT);
    }

    @Override
    public HttpResponse POST(String url, Map<String, String> requestHeaders, String content) {
        try {
            URL u = new URL(url);
            HttpURLConnection c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod(HttpMethod.POST.toString());
            for (Entry<String, String> entry : requestHeaders.entrySet()) {
                c.setRequestProperty(entry.getKey(), entry.getValue());
            }
            c.setDoOutput(true);
            c.setDoInput(true);
            try (OutputStream out = c.getOutputStream()) {
                out.write(content.getBytes(StandardCharsets.UTF_8));
            }
            String text = Util.readString(c.getInputStream(), StandardCharsets.UTF_8);
            return new HttpResponse(c.getResponseCode(), text);
        } catch (IOException e) {
            throw new ClientException(e);
        }
    }

    @Override
    public Path getBasePath() {
        return basePath;
    }

    private static HttpResponse patch(String url, Map<String, String> requestHeaders, String content,
            HttpMethod method) {
        try {
            URL u = new URL(url);
            HttpURLConnection c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod(method.toString());
            for (Entry<String, String> entry : requestHeaders.entrySet()) {
                c.setRequestProperty(entry.getKey(), entry.getValue());
            }
            c.setDoOutput(true);
            c.setDoInput(false);
            try (OutputStream out = c.getOutputStream()) {
                out.write(content.getBytes(StandardCharsets.UTF_8));
            }
            // don't disconnect c so socket can be reused?
            return new HttpResponse(c.getResponseCode(), null);
        } catch (IOException e) {
            throw new ClientException(e);
        }
    }

}
