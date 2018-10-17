package com.github.davidmoten.odata.client.internal;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
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
import com.github.davidmoten.odata.client.RequestHeader;

public class ApacheHttpClientHttpService implements HttpService {

    private final Path basePath;
    private final CloseableHttpClient client;
    private final Function<List<RequestHeader>, List<RequestHeader>> requestHeadersModifier;

    public ApacheHttpClientHttpService(Path basePath, Supplier<CloseableHttpClient> clientSupplier,
            Function<List<RequestHeader>, List<RequestHeader>> requestHeadersModifier) {
        this.basePath = basePath;
        this.client = clientSupplier.get();
        this.requestHeadersModifier = requestHeadersModifier;
    }

    public ApacheHttpClientHttpService(Path basePath) {
        this(basePath, () -> HttpClientBuilder.create().useSystemProperties().build(), m -> m);
    }

    @Override
    public HttpResponse GET(String url, List<RequestHeader> requestHeaders) {
        return getResponse(requestHeaders, new HttpGet(url), true, null);
    }

    @Override
    public HttpResponse PATCH(String url, List<RequestHeader> requestHeaders, String content) {
        return getResponse(requestHeaders, new HttpPatch(url), false, content);
    }

    @Override
    public HttpResponse PUT(String url, List<RequestHeader> requestHeaders, String content) {
        return getResponse(requestHeaders, new HttpPut(url), false, content);
    }

    @Override
    public HttpResponse POST(String url, List<RequestHeader> requestHeaders, String content) {
        return getResponse(requestHeaders, new HttpGet(url), true, content);
    }

    @Override
    public HttpResponse DELETE(String url, List<RequestHeader> requestHeaders) {
        return getResponse(requestHeaders, new HttpGet(url), false, null);
    }

    @Override
    public Path getBasePath() {
        return basePath;
    }

    private HttpResponse getResponse(List<RequestHeader> requestHeaders, HttpUriRequest request, boolean doInput,
            String content) {
        System.out.println(request.getMethod() + " from url " + request.getURI());
        for (RequestHeader header : requestHeadersModifier.apply(requestHeaders)) {
            request.addHeader(header.name(), header.value());
        }
        try {
            if (content != null && request instanceof HttpEntityEnclosingRequest) {
                ((HttpEntityEnclosingRequest) request).setEntity(new StringEntity(content));
            }
            System.out.println("executing request");
            org.apache.http.HttpResponse response = client.execute(request);
            System.out.println("executed request, code=" + response.getStatusLine().getStatusCode());
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

    @Override
    public void close() throws Exception {
        client.close();
    }

}
