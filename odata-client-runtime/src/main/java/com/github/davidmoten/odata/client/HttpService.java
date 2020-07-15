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

    HttpResponse get(String url, List<RequestHeader> requestHeaders, HttpRequestOptions options);

    HttpResponse patch(String url, List<RequestHeader> requestHeaders, InputStream content, HttpRequestOptions options);

    HttpResponse put(String url, List<RequestHeader> requestHeaders, InputStream content, HttpRequestOptions options);

    HttpResponse post(String url, List<RequestHeader> requestHeaders, InputStream content, HttpRequestOptions options);

    HttpResponse delete(String url, List<RequestHeader> requestHeaders, HttpRequestOptions options);

    InputStream getStream(String url, List<RequestHeader> requestHeaders, HttpRequestOptions options);

    default Optional<Proxy> getProxy() {
        return Optional.empty();
    }

    Path getBasePath();

    default HttpResponse patch(String url, List<RequestHeader> requestHeaders, String content, HttpRequestOptions options) {
        try (InputStream in = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
            return patch(url, requestHeaders, in, options);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    default HttpResponse put(String url, List<RequestHeader> requestHeaders, String content, HttpRequestOptions options) {
        try (InputStream in = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
            return put(url, requestHeaders, in, options);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    default HttpResponse post(String url, List<RequestHeader> requestHeaders, String content, HttpRequestOptions options) {
        try (InputStream in = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
            return post(url, requestHeaders, in, options);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    default HttpResponse submitWithContent(HttpMethod method, String url,
            List<RequestHeader> requestHeaders, InputStream content, HttpRequestOptions options) {
        if (method == HttpMethod.PATCH) {
            return patch(url, requestHeaders, content, options);
        } else if (method == HttpMethod.PUT) {
            return put(url, requestHeaders, content, options);
        } else if (method == HttpMethod.POST) {
            return put(url, requestHeaders, content, options);
        } else {
            throw new IllegalArgumentException(
                    method + " not permitted for a submission with content");
        }
    }

    default HttpResponse submitWithContent(HttpMethod method, String url,
            List<RequestHeader> requestHeaders, String content, HttpRequestOptions options) {
        try (InputStream in = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
            return submitWithContent(method, url, requestHeaders, in, options);
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
