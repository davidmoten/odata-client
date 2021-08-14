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

    Path getBasePath();

    HttpResponse submit(HttpMethod method, String url, List<RequestHeader> requestHeaders,
            InputStream content, int length, HttpRequestOptions options);

    InputStream getStream(HttpMethod method, String url, List<RequestHeader> requestHeaders,
            HttpRequestOptions options);

    default HttpResponse submit(HttpMethod method, String url, List<RequestHeader> requestHeaders,
            HttpRequestOptions options) {
        return submit(method, url, requestHeaders, null, LENGTH_UNKNOWN, options);
    }

    default HttpResponse get(String url, List<RequestHeader> requestHeaders,
            HttpRequestOptions options) {
        return submit(HttpMethod.GET, url, requestHeaders, options);
    }

    default HttpResponse patch(String url, List<RequestHeader> requestHeaders, InputStream content,
            int length, HttpRequestOptions options) {
        return submit(HttpMethod.PATCH, url, requestHeaders, content, length, options);
    }

    default HttpResponse put(String url, List<RequestHeader> requestHeaders, InputStream content,
            int length, HttpRequestOptions options) {
        return submit(HttpMethod.PUT, url, requestHeaders, content, length, options);
    }

    default HttpResponse post(String url, List<RequestHeader> requestHeaders, InputStream content,
            int length, HttpRequestOptions options) {
        return submit(HttpMethod.POST, url, requestHeaders, content, length, options);
    }

    default HttpResponse delete(String url, List<RequestHeader> requestHeaders,
            HttpRequestOptions options) {
        return submit(HttpMethod.DELETE, url, requestHeaders, options);
    }

    default InputStream getStream(String url, List<RequestHeader> requestHeaders,
            HttpRequestOptions options) {
        return getStream(HttpMethod.GET, url, requestHeaders, options);
    }

    default Optional<Proxy> getProxy() {
        return Optional.empty();
    }

    default HttpResponse patch(String url, List<RequestHeader> requestHeaders, String content,
            HttpRequestOptions options) {
        byte[] b = content.getBytes(StandardCharsets.UTF_8);
        try (InputStream in = new ByteArrayInputStream(b)) {
            return patch(url, requestHeaders, in, b.length, options);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    default HttpResponse put(String url, List<RequestHeader> requestHeaders, String content,
            HttpRequestOptions options) {
        byte[] b = content.getBytes(StandardCharsets.UTF_8);
        try (InputStream in = new ByteArrayInputStream(b)) {
            return put(url, requestHeaders, in, b.length, options);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    default HttpResponse submit(HttpMethod method, String url, List<RequestHeader> requestHeaders,
            byte[] content, HttpRequestOptions options) {
        try (InputStream in = new ByteArrayInputStream(content)) {
            return submit(method, url, requestHeaders, in, content.length, options);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    default HttpResponse submit(HttpMethod method, String url, List<RequestHeader> requestHeaders,
            String content, HttpRequestOptions options) {
        byte[] b = content.getBytes(StandardCharsets.UTF_8);
        return submit(method, url, requestHeaders, b, options);
    }

    default HttpResponse post(String url, List<RequestHeader> requestHeaders, String content,
            HttpRequestOptions options) {
        return submit(HttpMethod.POST, url, requestHeaders, content, options);
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

    default byte[] getBytes(String url, List<RequestHeader> requestHeaders,
            HttpRequestOptions options) {
        return Util.toByteArray(getStream(url, requestHeaders, options));
    }

    default String getStringUtf8(String url, HttpRequestOptions options) {
        return getStringUtf8(url, Collections.emptyList(), options);
    }

    default String getStringUtf8(String url, List<RequestHeader> requestHeaders,
            HttpRequestOptions options) {
        return new String(getBytes(url, requestHeaders, options), StandardCharsets.UTF_8);
    }

    static HttpService createDefaultService(Path path,
            Function<List<RequestHeader>, List<RequestHeader>> requestHeadersModifier,
            Consumer<HttpURLConnection> consumer) {
        return new DefaultHttpService(path, requestHeadersModifier, consumer);
    }

}
