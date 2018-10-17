package com.github.davidmoten.odata.client.internal;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
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
    private final Function<Map<String, String>, Map<String, String>> requestHeadersModifier;

    public ApacheHttpClientHttpService(Path basePath, Supplier<CloseableHttpClient> clientSupplier,
            Function<Map<String, String>, Map<String, String>> requestHeadersModifier) {
        this.basePath = basePath;
        this.client = clientSupplier.get();
        this.requestHeadersModifier = requestHeadersModifier;
    }

    public ApacheHttpClientHttpService(Path basePath) {
        this(basePath, () -> HttpClientBuilder.create().build(), m -> m);
    }

    @Override
    public HttpResponse GET(String url, Map<String, String> requestHeaders) {
        return getResponse(requestHeaders, new HttpGet(url), true, null);
    }

    @Override
    public HttpResponse PATCH(String url, Map<String, String> requestHeaders, String content) {
        return getResponse(requestHeaders, new HttpPatch(url), false, content);
    }

    @Override
    public HttpResponse PUT(String url, Map<String, String> requestHeaders, String content) {
        return getResponse(requestHeaders, new HttpPut(url), false, content);
    }

    @Override
    public HttpResponse POST(String url, Map<String, String> requestHeaders, String content) {
        return getResponse(requestHeaders, new HttpGet(url), true, content);
    }

    @Override
    public HttpResponse DELETE(String url, Map<String, String> requestHeaders) {
        return getResponse(requestHeaders, new HttpGet(url), false, null);
    }

    @Override
    public Path getBasePath() {
        return basePath;
    }

    private HttpResponse getResponse(Map<String, String> requestHeaders, HttpUriRequest request, boolean doInput,
            String content) {

        for (Entry<String, String> entry : requestHeadersModifier.apply(requestHeaders).entrySet()) {
            request.addHeader(entry.getKey(), entry.getValue());
        }
        try {
            if (content != null && request instanceof HttpEntityEnclosingRequest) {
                ((HttpEntityEnclosingRequest) request).setEntity(new StringEntity(content));
            }
            org.apache.http.HttpResponse response = client.execute(request);
            final String text;
            if (doInput) {
                text = Util.readString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            } else {
                text = null;
            }
            return new HttpResponse(response.getStatusLine().getStatusCode(), text);
        } catch (IOException e) {
            throw new ClientException(e);
        }
    }

}
