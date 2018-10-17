package com.github.davidmoten.odata.client.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;

import com.github.davidmoten.odata.client.ClientException;
import com.github.davidmoten.odata.client.HttpMethod;
import com.github.davidmoten.odata.client.HttpResponse;
import com.github.davidmoten.odata.client.HttpService;
import com.github.davidmoten.odata.client.Path;
import com.github.davidmoten.odata.client.RequestHeader;

public final class DefaultHttpService implements HttpService {

    private final Path basePath;
    private final Function<List<RequestHeader>, List<RequestHeader>> requestHeadersModifier;

    public DefaultHttpService(Path basePath,
            Function<List<RequestHeader>, List<RequestHeader>> requestHeadersModifier) {
        this.basePath = basePath;
        this.requestHeadersModifier = requestHeadersModifier;
    }

    @Override
    public HttpResponse GET(String url, List<RequestHeader> requestHeaders) {
        return getResponse(url, requestHeaders, HttpMethod.GET, true, null);
    }

    @Override
    public HttpResponse PATCH(String url, List<RequestHeader> requestHeaders, String content) {
        throw new ClientException(
                "PATCH is not supported by java.net.URLConnection, use PUT instead (or use the HttpService implementation based on Apache HttpClient)");
    }

    @Override
    public HttpResponse PUT(String url, List<RequestHeader> requestHeaders, String content) {
        return getResponse(url, requestHeaders, HttpMethod.PUT, false, content);
    }

    @Override
    public HttpResponse POST(String url, List<RequestHeader> requestHeaders, String content) {
        return getResponse(url, requestHeaders, HttpMethod.POST, true, content);
    }

    @Override
    public HttpResponse DELETE(String url, List<RequestHeader> requestHeaders) {
        return getResponse(url, requestHeaders, HttpMethod.DELETE, false, null);
    }

    @Override
    public Path getBasePath() {
        return basePath;
    }

    private HttpResponse getResponse(String url, List<RequestHeader> requestHeaders, HttpMethod method, boolean doInput,
            String content) {
        try {
            URL u = new URL(url);
            HttpURLConnection c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod(method.toString());
            for (RequestHeader header : requestHeadersModifier.apply(requestHeaders)) {
                c.setRequestProperty(header.name(), header.value());
            }
            c.setDoInput(doInput);
            c.setDoOutput(content != null);
            if (content != null) {
                try (OutputStream out = c.getOutputStream()) {
                    out.write(content.getBytes(StandardCharsets.UTF_8));
                }
            }
            final String text;
            if (doInput) {
                text = Util.readString(c.getInputStream(), StandardCharsets.UTF_8);
            } else {
                text = null;
            }
            return new HttpResponse(c.getResponseCode(), text);
        } catch (IOException e) {
            throw new ClientException(e);
        }
    }
}
