package com.github.davidmoten.odata.client;

import static java.util.Arrays.asList;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class TestingService {

    private static final InputStream EMPTY = new InputStream() {
        @Override
        public int read() throws IOException {
            return -1;
        }
    };

    public static Builder baseUrl(String url) {
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return new Builder().baseUrl(url);
    }

    public static Builder pathStyle(PathStyle pathStyle) {
        return new Builder().pathStyle(pathStyle);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Response {
        public final String resource;
        public final int statusCode;

        public Response(String resource, int statusCode) {
            this.resource = resource;
            this.statusCode = statusCode;
        }

        public Response(String text) {
            this(text, HttpURLConnection.HTTP_OK);
        }

        @Override
        public String toString() {
            return "Response [resource=" + resource + ", statusCode=" + statusCode + "]";
        }
    }

    public static abstract class BuilderBase<T extends BuilderBase<?, R>, R> {
        final Map<String, Response> responses = new HashMap<>();
        final Map<String, String> requests = new HashMap<>();

        String baseUrl = "https://testing.com";
        PathStyle pathStyle = PathStyle.IDENTIFIERS_AS_SEGMENTS;

        @SuppressWarnings("unchecked")
        public T baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T pathStyle(PathStyle pathStyle) {
            this.pathStyle = pathStyle;
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        private T expectResponse(String path, String responseResourceName, HttpMethod method,
                int statusCode, RequestHeader... requestHeaders) {
            responses.put(toKey(method, baseUrl + path, asList(requestHeaders)),
                    new Response(responseResourceName, statusCode));
            return (T) this;
        }

        private String toUrl(String path) {
            final String url;
            if (path.startsWith("https://")) {
                url = path;
            } else {
                url = baseUrl + path;
            }
            return url;
        }

        public Builder<T, R> expectRequest(String path) {
            return new Builder<T, R>(this, path);
        }

        public static final class Builder<T extends BuilderBase<?, R>, R> {

            private final List<RequestHeader> requestHeaders = new ArrayList<>();
            private final BuilderBase<T, R> base;
            private Optional<String> payloadResourcePath = Optional.empty();
            private HttpMethod method = HttpMethod.GET;
            private final String path;
            private String responseResourcePath;
            private Integer statusCode;

            public Builder(BuilderBase<T, R> base, String path) {
                this.base = base;
                this.path = path;
            }

            public Builder<T, R> withPayload(String resourcePath) {
                this.payloadResourcePath = Optional.of(resourcePath);
                return this;
            }

            public Builder<T, R> withRequestHeaders(RequestHeader... requestHeaders) {
                this.requestHeaders.addAll(Arrays.asList(requestHeaders));
                return this;
            }

            public Builder<T, R> withMethod(HttpMethod method) {
                this.method = method;
                return this;
            }

            public Builder<T, R> withResponse(String resourcePath) {
                this.responseResourcePath = resourcePath;
                return this;
            }

            public Builder<T, R> withResponseStatusCode(int statusCode) {
                this.statusCode = statusCode;
                return this;
            }

            public Builder<T, R> withRequestHeadersStandard() {
                return withRequestHeaders(RequestHeader.ACCEPT_JSON_METADATA_MINIMAL,
                        RequestHeader.ODATA_VERSION);
            }

            private T add() {
                if (!payloadResourcePath.isPresent()) {
                    return base.expectResponse(path, responseResourcePath, method,
                            applyDefaultIfMissing(statusCode, method),
                            requestHeaders.toArray(new RequestHeader[] {}));
                } else {
                    return base.expectRequestAndResponse(path, payloadResourcePath.get(),
                            responseResourcePath, method, applyDefaultIfMissing(statusCode, method),
                            requestHeaders.toArray(new RequestHeader[] {}));
                }
            }

            public R build() {
                return add().build();
            }

            @SuppressWarnings("unchecked")
            public Builder<T, R> expectRequest(String path) {
                return (Builder<T, R>) add().expectRequest(path);
            }
        }

        private static int applyDefaultIfMissing(Integer statusCode, HttpMethod method) {
            if (statusCode != null) {
                return statusCode;
            } else if (method == HttpMethod.PUT || method == HttpMethod.PATCH
                    || method == HttpMethod.DELETE) {
                return HttpURLConnection.HTTP_NO_CONTENT;
            } else {
                return HttpURLConnection.HTTP_OK;
            }
        }

        @SuppressWarnings("unchecked")
        private T expectRequestAndResponse(String path, String requestResourceName,
                String responseResourceName, HttpMethod method, int statusCode,
                RequestHeader... requestHeaders) {
            String url = toUrl(path);
            requests.put(toKey(method, url, asList(requestHeaders)), requestResourceName);
            responses.put(toKey(method, url, asList(requestHeaders)),
                    new Response(responseResourceName, statusCode));
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T expectDelete(String path, int responseStatusCode,
                RequestHeader... requestHeaders) {
            String key = toKey(HttpMethod.DELETE, baseUrl + path, asList(requestHeaders));
            requests.put(key, "/empty.txt");
            responses.put(key, new Response(null, responseStatusCode));
            return (T) this;
        }

        public T expectDelete(String path, RequestHeader... requestHeaders) {
            return expectDelete(path, HttpURLConnection.HTTP_NO_CONTENT, requestHeaders);
        }

        private static String toKey(HttpMethod method, String url,
                Collection<RequestHeader> requestHeaders) {
            return method + "\n  " + url + "\n  " + requestHeaders.stream()
                    .map(x -> x.name() + "=" + x.value()).sorted().collect(Collectors.joining("|"));
        }

        private static void log(Object o) {
            System.out.println(o);
        }

        protected HttpService createService() {
            return new HttpService() {

                @Override
                public HttpResponse get(String url, List<RequestHeader> requestHeaders,
                        HttpRequestOptions options) {
                    logExpected();
                    String key = BuilderBase.toKey(HttpMethod.GET, url, requestHeaders);
                    log("Getting:\n" + key);
                    Response response = responses.get(key);
                    String resourceName = response == null ? null : response.resource;
                    if (resourceName == null) {
                        throw new RuntimeException("GET response not found for url=" + url
                                + ", headers=" + requestHeaders);
                    }
                    try {
                        URL resource = TestingService.class.getResource(resourceName);
                        if (resource == null) {
                            throw new RuntimeException(
                                    "resource not found on classpath: " + resourceName);
                        }
                        byte[] bytes = Files.readAllBytes(Paths.get(resource.toURI()));
                        return new HttpResponse(response.statusCode, bytes);
                    } catch (IOException | URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                }

                private void logExpected() {
                    log("Expected responses:");
                    responses.entrySet().forEach(r -> log(r.getKey() + "\n=>" + r.getValue()));
                }

                @Override
                public HttpResponse patch(String url, List<RequestHeader> requestHeaders,
                        InputStream content, int length, HttpRequestOptions options) {
                    return postPatchPutOrDelete(url, requestHeaders, content, length, options,
                            HttpMethod.PATCH);
                }

                @Override
                public HttpResponse put(String url, List<RequestHeader> requestHeaders,
                        InputStream content, int length, HttpRequestOptions options) {
                    return postPatchPutOrDelete(url, requestHeaders, content, length, options,
                            HttpMethod.PUT);
                }

                @Override
                public HttpResponse post(String url, List<RequestHeader> requestHeaders,
                        InputStream content, int length, HttpRequestOptions options) {
                    return postPatchPutOrDelete(url, requestHeaders, content, length, options,
                            HttpMethod.POST);
                }

                @Override
                public HttpResponse delete(String url, List<RequestHeader> requestHeaders,
                        HttpRequestOptions options) {
                    return postPatchPutOrDelete(url, requestHeaders, EMPTY, 0, options,
                            HttpMethod.DELETE);
                }

                private HttpResponse postPatchPutOrDelete(String url,
                        List<RequestHeader> requestHeaders, InputStream content, int length,
                        HttpRequestOptions options, HttpMethod method) {
                    log(method + " called at " + url);
                    String text = Util.utf8(content);
                    log(text);
                    logExpected();
                    log("Calling:");
                    String key = BuilderBase.toKey(method, url, requestHeaders);
                    log(key);
                    String requestResourceName = requests.get(key);
                    if (requestResourceName == null) {
                        throw new RuntimeException(method + " request not expected for url=" + url
                                + ", headers=" + requestHeaders);
                    }
                    try {
                        System.out.println("requestResourceName=" + requestResourceName);
                        byte[] requestExpected = readResource(url, requestResourceName);
                        String expectedText = new String(requestExpected, StandardCharsets.UTF_8);
                        if (expectedText.equals(text)
                                || Serializer.INSTANCE.matches(expectedText, text)) {
                            Response resp = responses
                                    .get(BuilderBase.toKey(method, url, requestHeaders));
                            String responseResourceName = resp.resource;
                            final byte[] responseExpected;
                            if (responseResourceName == null) {
                                responseExpected = null;
                            } else {
                                responseExpected = readResource(url, responseResourceName);
                            }
//                            final int responseCode;
//                            if (resp.statusCode != HttpURLConnection.HTTP_OK) {
//                                responseCode = resp.statusCode;
//                            } else {
//                                responseCode = url.contains("delta") ? HttpURLConnection.HTTP_OK
//                                        : HttpURLConnection.HTTP_CREATED;
//                            }
                            return new HttpResponse(resp.statusCode, responseExpected);
                        } else {
                            throw new RuntimeException(
                                    "request does not match expected.\n==== Received ====\n" + text
                                            + "\n==== Expected =====\n" + expectedText);
                        }
                    } catch (IOException | URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public Path getBasePath() {
                    return new Path(baseUrl, pathStyle);
                }

                @Override
                public void close() throws Exception {
                    // do nothing
                }

                @Override
                public InputStream getStream(String url, List<RequestHeader> requestHeaders,
                        HttpRequestOptions options) {
                    HttpResponse h = get(url, requestHeaders, options);
                    return new ByteArrayInputStream(h.getBytes());
                }

                @Override
                public InputStream getStream(HttpMethod method, String url,
                        List<RequestHeader> requestHeaders, HttpRequestOptions options) {
                    throw new UnsupportedOperationException();
                }

            };
        }

        public abstract R build();
    }

    public static abstract class ContainerBuilder<T> extends BuilderBase<ContainerBuilder<T>, T> {

        public abstract T _create(Context context);

        private final Map<String, Object> properties = new HashMap<>();
        private final List<SchemaInfo> schemas = new ArrayList<>();

        @Override
        public T build() {
            return _create(new Context(Serializer.INSTANCE, createService(), properties, schemas));
        }

        public ContainerBuilder<T> addSchema(SchemaInfo schema) {
            this.schemas.add(schema);
            return this;
        }

        public ContainerBuilder<T> addProperty(String name, Object value) {
            properties.put(name, value);
            return this;
        }

        public ContainerBuilder<T> addProperties(Map<String, Object> p) {
            properties.putAll(p);
            return this;
        }

    }

    public static final class Builder extends BuilderBase<Builder, HttpService> {

        @Override
        public HttpService build() {
            return createService();
        }
    }

    private static byte[] readResource(String url, String resourceName)
            throws IOException, URISyntaxException {
        if (resourceName == null) {
            throw new RuntimeException("resource not found for url=" + url);
        }
        try (InputStream in = TestingService.class.getResourceAsStream(resourceName)) {
            return Util.toByteArray(in);
        }
    }

}
