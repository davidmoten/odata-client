package com.github.davidmoten.odata.client.internal;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpMessage;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.github.davidmoten.odata.client.ClientException;
import com.github.davidmoten.odata.client.HttpResponse;
import com.github.davidmoten.odata.client.HttpService;
import com.github.davidmoten.odata.client.Path;

public class ApacheHttpClientHttpService implements HttpService {

    private final Path basePath;
    private final CloseableHttpClient client;

    public ApacheHttpClientHttpService(Path basePath, Supplier<CloseableHttpClient> clientSupplier) {
        this.basePath = basePath;
        this.client = clientSupplier.get();
    }

    public ApacheHttpClientHttpService(Path basePath) {
        this(basePath, () -> HttpClientBuilder.create().build());
    }

    @Override
    public HttpResponse GET(String url, Map<String, String> requestHeaders) {
        return getResponse(requestHeaders, new HttpGet(url));
    }

    @Override
    public HttpResponse PATCH(String url, Map<String, String> requestHeaders, String content) {
        return getResponseFromRequestWithContent(requestHeaders, content, new HttpPatch(url));
    }

    @Override
    public HttpResponse PUT(String url, Map<String, String> requestHeaders, String content) {
        return getResponseFromRequestWithContent(requestHeaders, content, new HttpPut(url));
    }

    @Override
    public HttpResponse POST(String url, Map<String, String> requestHeaders, String content) {
        return getResponseFromRequestWithContent(requestHeaders, content, new HttpPost(url));
    }

    @Override
    public HttpResponse DELETE(String url, Map<String, String> requestHeaders) {
        return getResponse(requestHeaders, new HttpDelete(url));
    }

    @Override
    public Path getBasePath() {
        return basePath;
    }

    private HttpResponse getResponse(Map<String, String> requestHeaders, HttpUriRequest h) {
        addHeaders(h, requestHeaders);
        try {
            org.apache.http.HttpResponse response = client.execute(h);
            return new HttpResponse(response.getStatusLine().getStatusCode(),
                    Util.readString(response.getEntity().getContent(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new ClientException(e);
        }
    }

    private <T extends HttpEntityEnclosingRequest & HttpUriRequest> HttpResponse getResponseFromRequestWithContent(
            Map<String, String> requestHeaders, String content, T h) {
        try {
            h.setEntity(new StringEntity(content));
        } catch (UnsupportedEncodingException e) {
            throw new ClientException(e);
        }
        return getResponse(requestHeaders, h);
    }

    private static void addHeaders(HttpMessage m, Map<String, String> requestHeaders) {
        for (Entry<String, String> entry : requestHeaders.entrySet()) {
            m.addHeader(entry.getKey(), entry.getValue());
        }
    }
}
