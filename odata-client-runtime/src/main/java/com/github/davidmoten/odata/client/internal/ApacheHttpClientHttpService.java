package com.github.davidmoten.odata.client.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.odata.client.ClientException;
import com.github.davidmoten.odata.client.HttpMethod;
import com.github.davidmoten.odata.client.HttpRequestOptions;
import com.github.davidmoten.odata.client.HttpResponse;
import com.github.davidmoten.odata.client.HttpService;
import com.github.davidmoten.odata.client.Path;
import com.github.davidmoten.odata.client.RequestHeader;

public class ApacheHttpClientHttpService implements HttpService {

    private static final Logger log = LoggerFactory.getLogger(ApacheHttpClientHttpService.class);

    private final Path basePath;
    private final CloseableHttpClient client;
    private final BiFunction<URL, List<RequestHeader>, List<RequestHeader>> requestHeadersModifier;

    public ApacheHttpClientHttpService(Path basePath, Supplier<CloseableHttpClient> clientSupplier,
            BiFunction<URL, List<RequestHeader>, List<RequestHeader>> requestHeadersModifier) {
        this.basePath = basePath;
        this.client = clientSupplier.get();
        this.requestHeadersModifier = requestHeadersModifier;
    }

    public ApacheHttpClientHttpService(Path basePath) {
        this(basePath, () -> HttpClientBuilder.create().useSystemProperties().build(),
                (url, m) -> m);
    }

    @Override
    public HttpResponse get(String url, List<RequestHeader> requestHeaders,
            HttpRequestOptions options) {
        return getResponse(requestHeaders, new HttpGet(url), true, null, -1, options);
    }

    @Override
    public HttpResponse patch(String url, List<RequestHeader> requestHeaders, InputStream content,
            int length, HttpRequestOptions options) {
        return getResponse(requestHeaders, new HttpPatch(url), false, content, length, options);
    }

    @Override
    public HttpResponse put(String url, List<RequestHeader> requestHeaders, InputStream content,
            int length, HttpRequestOptions options) {
        return getResponse(requestHeaders, new HttpPut(url), false, content, length, options);
    }

    @Override
    public HttpResponse post(String url, List<RequestHeader> requestHeaders, InputStream content,
            int length, HttpRequestOptions options) {
        return getResponse(requestHeaders, new HttpPost(url), true, content, length, options);
    }

    @Override
    public HttpResponse delete(String url, List<RequestHeader> requestHeaders,
            HttpRequestOptions options) {
        return getResponse(requestHeaders, new HttpDelete(url), false, null,
                HttpService.LENGTH_UNKNOWN, options);
    }

    @Override
    public Path getBasePath() {
        return basePath;
    }

    private static URL toUrl(HttpRequestBase request) {
        try {
            return request.getURI().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private HttpResponse getResponse(List<RequestHeader> requestHeaders, HttpRequestBase request,
            boolean doInput, InputStream content, int length, HttpRequestOptions options) {
        Preconditions.checkNotNull(options);
        log.debug("{} from url {}", request.getMethod(), request.getURI());
        log.debug("requestHeaders={}", requestHeaders);
        for (RequestHeader header : requestHeadersModifier.apply(toUrl(request), requestHeaders)) {
            request.addHeader(header.name(), header.value());
        }
        try {
            if (content != null && request instanceof HttpEntityEnclosingRequest) {
                ((HttpEntityEnclosingRequest) request)
                        .setEntity(new InputStreamEntity(content, length));
                log.debug("content={}", content);
            }
            RequestConfig config = com.github.davidmoten.odata.client.Util.nvl(request.getConfig(),
                    RequestConfig.DEFAULT);
            Builder builder = RequestConfig //
                    .copy(config);
            options.requestConnectTimeoutMs()
                    .ifPresent(x -> builder.setConnectTimeout(x.intValue()));
            options.requestReadTimeoutMs().ifPresent(x -> builder.setSocketTimeout(x.intValue()));
            config = builder.build();
            request.setConfig(config);
            log.debug("executing request");
            try (CloseableHttpResponse response = client.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                log.debug("executed request, code={}", statusCode);
                final String text;
                if (doInput || isError(statusCode)) {
                    text = Util.readString(response.getEntity().getContent(),
                            StandardCharsets.UTF_8);
                } else {
                    text = null;
                }
                log.debug("response text=\n{}", text);
                return new HttpResponse(statusCode, text);
            }
        } catch (IOException e) {
            throw new ClientException(e);
        }
    }

    @Override
    public InputStream getStream(HttpMethod method, String url, List<RequestHeader> requestHeaders,
            HttpRequestOptions options) {
        HttpRequestBase b = toRequestBase(method, url);
        return getStream(requestHeaders, b, null, 0, options);
    }

    private static HttpRequestBase toRequestBase(HttpMethod method, String url) {
        if (method == HttpMethod.GET) {
            return new HttpGet(url);
        } else if (method == HttpMethod.DELETE) {
            return new HttpDelete(url);
        } else if (method == HttpMethod.PATCH) {
            return new HttpPatch(url);
        } else if (method == HttpMethod.PUT) {
            return new HttpPut(url);
        } else if (method == HttpMethod.POST) {
            return new HttpPut(url);
        } else {
            throw new UnsupportedOperationException(method.toString() + " not recognized");
        }
    }

    public InputStream getStream(List<RequestHeader> requestHeaders, HttpRequestBase request,
            InputStream content, int length, HttpRequestOptions options) {
        Preconditions.checkNotNull(options);
        log.debug("{} from url {}", request.getMethod(), request.getURI());
        log.debug("requestHeaders={}", requestHeaders);
        boolean contentLengthSet = false;
        for (RequestHeader header : requestHeadersModifier.apply(toUrl(request), requestHeaders)) {
            request.addHeader(header.name(), header.value());
            if ("Content-Length".equals(header.name())) {
                contentLengthSet = true;
            }
        }
        if (content != null && !contentLengthSet) {
            request.addHeader("Content-Length", Integer.toString(length));
        }
        try {
            if (content != null && request instanceof HttpEntityEnclosingRequest) {
                ((HttpEntityEnclosingRequest) request)
                        .setEntity(new InputStreamEntity(content, length));
                log.debug("content={}", content);
            }
            RequestConfig config = com.github.davidmoten.odata.client.Util.nvl(request.getConfig(),
                    RequestConfig.DEFAULT);
            Builder builder = RequestConfig //
                    .copy(config);
            options.requestConnectTimeoutMs()
                    .ifPresent(x -> builder.setConnectTimeout(x.intValue()));
            options.requestReadTimeoutMs().ifPresent(x -> builder.setSocketTimeout(x.intValue()));
            config = builder.build();
            request.setConfig(config);
            log.debug("executing request");
            try (CloseableHttpResponse response = client.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                log.debug("executed request, code={}", statusCode);
                InputStream is = response.getEntity().getContent();
                InputStream in = new InputStreamWithCloseable(is, response);
                if (!isOk(statusCode)) {
                    try {
                        String msg = Util.readString(in, StandardCharsets.UTF_8);
                        throw new ClientException(statusCode,
                                "getStream returned HTTP " + statusCode + "\n" //
                                        + "url=" + request.getURI() + "\n" //
                                        + "headers=" + requestHeaders + "\n" //
                                        + "response:\n" //
                                        + msg);
                    } finally {
                        in.close();
                    }
                } else {
                    return in;
                }

            }
        } catch (IOException e) {
            throw new ClientException(e);
        }
    }

    private static boolean isError(int statusCode) {
        return statusCode >= 400;
    }

    @Override
    public void close() throws Exception {
        log.info("closing client");
        client.close();
        log.info("closed client");
    }

    private static boolean isOk(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }

}
