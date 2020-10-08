package com.github.davidmoten.odata.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import com.github.davidmoten.odata.client.internal.DefaultHttpService;

public interface HttpService extends AutoCloseable {
    
    static int LENGTH_UNKNOWN = -1;

    HttpResponse get(String url, List<RequestHeader> requestHeaders, HttpRequestOptions options);

    HttpResponse patch(String url, List<RequestHeader> requestHeaders, InputStream content, int length, HttpRequestOptions options);

    HttpResponse put(String url, List<RequestHeader> requestHeaders, InputStream content, int length, HttpRequestOptions options);

    HttpResponse post(String url, List<RequestHeader> requestHeaders, InputStream content, int length, HttpRequestOptions options);
    
    HttpResponse delete(String url, List<RequestHeader> requestHeaders, HttpRequestOptions options);

    InputStream getStream(String url, List<RequestHeader> requestHeaders, HttpRequestOptions options);

    default Optional<Proxy> getProxy() {
        return Optional.empty();
    }

    Path getBasePath();
    
    
    default HttpResponse send(HttpMethod method, String url, List<RequestHeader> requestHeaders,
            InputStream content, int length, HttpRequestOptions options) {
        if (method == HttpMethod.PATCH) {
            return patch(url, requestHeaders, content, length, options);
        } else if (method == HttpMethod.POST) {
            return post(url, requestHeaders, content, length, options);
        } else if (method == HttpMethod.PUT) {
            return put(url, requestHeaders, content, length, options);
        } else {
            throw new ClientException(
                    "method not supported for update: " + method + ", url=" + url);
        }
    }

    default HttpResponse patch(String url, List<RequestHeader> requestHeaders, String content, HttpRequestOptions options) {
        byte[] b = content.getBytes(StandardCharsets.UTF_8);
        try (InputStream in = new ByteArrayInputStream(b)) {
            return patch(url, requestHeaders, in, b.length, options);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    default HttpResponse put(String url, List<RequestHeader> requestHeaders, String content, HttpRequestOptions options) {
        byte[] b = content.getBytes(StandardCharsets.UTF_8);
        try (InputStream in = new ByteArrayInputStream(b)) {
            return put(url, requestHeaders, in, b.length, options);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    default HttpResponse post(String url, List<RequestHeader> requestHeaders, String content, HttpRequestOptions options) {
        byte[] b = content.getBytes(StandardCharsets.UTF_8);
        try (InputStream in = new ByteArrayInputStream(b)) {
            return post(url, requestHeaders, in, b.length, options);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    default HttpResponse submitWithContent(HttpMethod method, String url,
            List<RequestHeader> requestHeaders, InputStream content, int length, HttpRequestOptions options) {
        if (method == HttpMethod.PATCH) {
            return patch(url, requestHeaders, content, length, options);
        } else if (method == HttpMethod.PUT) {
            return put(url, requestHeaders, content, length, options);
        } else if (method == HttpMethod.POST) {
            return post(url, requestHeaders, content, length, options);
        } else {
            throw new IllegalArgumentException(
                    method + " not permitted for a submission with content");
        }
    }

    default HttpResponse submitWithContent(HttpMethod method, String url,
            List<RequestHeader> requestHeaders, String content, HttpRequestOptions options) {
        byte[] b = content.getBytes(StandardCharsets.UTF_8);
        try (InputStream in = new ByteArrayInputStream(b)) {
            return submitWithContent(method, url, requestHeaders, in, b.length, options);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    default HttpResponse get(String url, HttpRequestOptions options) {
        return get(url, Collections.emptyList(), options);
    }

    default InputStream getStream(String url, HttpRequestOptions options) {
        return getStream(url, Collections.emptyList(), options);
    }

    default byte[] getBytes(String url, HttpRequestOptions options) {
        return getBytes(url, Collections.emptyList(), options);
    }

    default byte[] getBytes(String url, List<RequestHeader> requestHeaders, HttpRequestOptions options) {
        return Util.toByteArray(getStream(url, requestHeaders, options));
    }

    default String getStringUtf8(String url, HttpRequestOptions options) {
        return getStringUtf8(url, Collections.emptyList(), options);
    }

    default String getStringUtf8(String url, List<RequestHeader> requestHeaders, HttpRequestOptions options) {
        return new String(getBytes(url, requestHeaders, options), StandardCharsets.UTF_8);
    }

    static HttpService createDefaultService(Path path,
                                            Function<List<RequestHeader>, List<RequestHeader>> requestHeadersModifier,
                                            Consumer<HttpURLConnection> consumer) {
        return new DefaultHttpService(path, requestHeadersModifier, consumer);
    }

}
