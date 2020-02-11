package com.github.davidmoten.odata.client.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.davidmoten.odata.client.ClientException;
import com.github.davidmoten.odata.client.HttpResponse;
import com.github.davidmoten.odata.client.HttpService;
import com.github.davidmoten.odata.client.Path;
import com.github.davidmoten.odata.client.RequestHeader;

public class ApacheHttpClientHttpService implements HttpService {

    private static final Logger log = LoggerFactory.getLogger(ApacheHttpClientHttpService.class);

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
    public HttpResponse get(String url, List<RequestHeader> requestHeaders) {
        return getResponse(requestHeaders, new HttpGet(url), true, null);
    }

    @Override
    public HttpResponse patch(String url, List<RequestHeader> requestHeaders, String content) {
        return getResponse(requestHeaders, new HttpPatch(url), false, content);
    }

    @Override
    public HttpResponse put(String url, List<RequestHeader> requestHeaders, String content) {
        return getResponse(requestHeaders, new HttpPut(url), false, content);
    }

    @Override
    public HttpResponse post(String url, List<RequestHeader> requestHeaders, String content) {
        return getResponse(requestHeaders, new HttpPost(url), true, content);
    }

    @Override
    public HttpResponse delete(String url, List<RequestHeader> requestHeaders) {
        return getResponse(requestHeaders, new HttpDelete(url), false, null);
    }

    @Override
    public Path getBasePath() {
        return basePath;
    }

    private HttpResponse getResponse(List<RequestHeader> requestHeaders, HttpUriRequest request,
            boolean doInput, String content) {
        log.debug("{} from url {}", request.getMethod(), request.getURI());
        for (RequestHeader header : requestHeadersModifier.apply(requestHeaders)) {
            request.addHeader(header.name(), header.value());
        }
        try {
            if (content != null && request instanceof HttpEntityEnclosingRequest) {
                ((HttpEntityEnclosingRequest) request).setEntity(new StringEntity(content));
                log.debug("content={}", content);
            }
            log.debug("executing request");
            try (CloseableHttpResponse response = client.execute(request)) {
                log.debug("executed request, code={}", response.getStatusLine().getStatusCode());
                final String text;
                if (doInput) {
                    text = Util.readString(response.getEntity().getContent(),
                            StandardCharsets.UTF_8);
                } else {
                    text = null;
                }
                log.debug("response text=\n{}", text);
                return new HttpResponse(response.getStatusLine().getStatusCode(), text);
            }
        } catch (IOException e) {
            throw new ClientException(e);
        }
    }

    @Override
    public void close() throws Exception {
        log.info("closing client");
        client.close();
        log.info("closed client");
    }

    @Override
    public InputStream getStream(String url, List<RequestHeader> requestHeaders) {
        // note follow redirects by default
        HttpGet request = new HttpGet(url);
        log.debug("{} from url {}", request.getMethod(), request.getURI());
        for (RequestHeader header : requestHeadersModifier.apply(requestHeaders)) {
            request.addHeader(header.name(), header.value());
        }
        try {
            log.debug("executing request");
            final CloseableHttpResponse response = client.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            log.debug("executed request, code={}", response.getStatusLine().getStatusCode());
            InputStream is = response.getEntity().getContent();
            InputStream in = new InputStream() {
                @Override
                public int read() throws IOException {
                    return is.read();
                }

                @Override
                public void close() throws IOException {
                    try {
                        is.close();
                    } finally {
                        response.close();
                    }
                }
            };
            if (!isOk(statusCode)) {
                try {
                    String msg = Util.readString(in, StandardCharsets.UTF_8);
                    throw new ClientException("getStream returned HTTP " + statusCode + "\n"  //
                            + "url=" + url + "\n" //
                            + "headers=" + requestHeaders + "\n"
                            + "response:\n" //
                            + msg);
                } finally {
                    in.close();
                }
            } else {
                return in;
            }
        } catch (IOException e) {
            throw new ClientException(e);
        }
    }

    private static boolean isOk(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }

}
