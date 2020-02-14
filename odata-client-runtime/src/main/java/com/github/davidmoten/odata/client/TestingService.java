package com.github.davidmoten.odata.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TestingService {
    
    public static Builder replyWithResource(String path, String resourceName) {
        return new Builder().replyWithResource(path, resourceName);
    }

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

    public static abstract class BuilderBase<T extends BuilderBase<?, R>, R> {
        Map<String, String> responses = new HashMap<>();
        Map<String, String> requests = new HashMap<>();

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

        public T replyWithResource(String path, String resourceName) {
            return replyWithResource(path, resourceName, HttpMethod.GET);
        }

        @SuppressWarnings("unchecked")
        public T replyWithResource(String path, String resourceName, HttpMethod method) {
            responses.put(toKey(method, baseUrl + path), resourceName);
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T expectRequest(String path, String resourceName, HttpMethod method) {
            requests.put(toKey(method, baseUrl + path), resourceName);
            return (T) this;
        }
        
        @SuppressWarnings("unchecked")
        public T expectRequestAndReply(String path, String resourceName, String replyResourceName, HttpMethod method) {
            requests.put(toKey(method, baseUrl + path), resourceName);
            responses.put(toKey(method, baseUrl+ path), replyResourceName);
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T expectDelete(String path) {
            requests.put(toKey(HttpMethod.DELETE, baseUrl + path), "DELETE");
            return (T) this;
        }

        private static String toKey(HttpMethod method, String url) {
            return method + "_" + url;
        }
        
        private static final void log(Object o) {
            System.out.println(String.valueOf(o));
        }

        protected HttpService createService() {
            return new HttpService() {

                @Override
                public HttpResponse get(String url, List<RequestHeader> requestHeaders) {
                    responses.entrySet().forEach(r -> log(r));
                    String resourceName = responses.get(BuilderBase.toKey(HttpMethod.GET, url));
                    if (resourceName == null) {
                        throw new RuntimeException("GET response not found for url=" + url);
                    }
                    try {
                        URL resource = TestingService.class.getResource(resourceName);
                        if (resource == null) {
                            throw new RuntimeException("resource not found on classpath: " + resourceName);
                        }
                        String text = new String(Files.readAllBytes(Paths.get(resource.toURI())));
                        return new HttpResponse(200, text);
                    } catch (IOException | URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public HttpResponse patch(String url, List<RequestHeader> requestHeaders, String text) {
                    log("PATCH called at" + url);
                    log(text);
                    String resourceName = requests.get(BuilderBase.toKey(HttpMethod.PATCH, url));
                    if (resourceName == null) {
                        throw new RuntimeException("PATCH response not found for url=" + url);
                    }
                    try {
                        String expected = new String(
                                Files.readAllBytes(Paths.get(TestingService.class.getResource(resourceName).toURI())));

                        if (Serializer.INSTANCE.matches(expected, text)) {
                            return new HttpResponse(HttpURLConnection.HTTP_NO_CONTENT, null);
                        } else {
                            throw new RuntimeException("request does not match expected.\n==== Recieved ====\n" + text
                                    + "\n==== Expected =====\n" + expected);
                        }
                    } catch (IOException | URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public HttpResponse put(String url, List<RequestHeader> h, String json) {
                    return patch(url, h, json);
                }

                @Override
                public HttpResponse post(String url, List<RequestHeader> requestHeaders, String text) {
                    log("POST called at " + url);
                    log(text);
                    String requestResourceName = requests.get(BuilderBase.toKey(HttpMethod.POST, url));

                    try {
                        String requestExpected = readResource(url, requestResourceName);
                        if (Serializer.INSTANCE.matches(requestExpected, text)) {
                            String responseResourceName = responses.get(BuilderBase.toKey(HttpMethod.POST, url));
                            String responseExpected = readResource(url, responseResourceName);
                            return new HttpResponse(HttpURLConnection.HTTP_CREATED, responseExpected);
                        } else {
                            throw new RuntimeException("request does not match expected.\n==== Received ====\n" + text
                                    + "\n==== Expected =====\n" + requestExpected);
                        }
                    } catch (IOException | URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public HttpResponse delete(String url, List<RequestHeader> requestHeaders) {
                    log("DELETE called at\n" + url);
                    String resourceName = requests.get(BuilderBase.toKey(HttpMethod.DELETE, url));
                    if (resourceName == null) {
                        throw new RuntimeException("DELETE request not expected for url=" + url);
                    }
                    return new HttpResponse(HttpURLConnection.HTTP_NO_CONTENT, null);
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
                public InputStream getStream(String url, List<RequestHeader> requestHeaders) {
                    HttpResponse h = get(url, requestHeaders);
                    return new ByteArrayInputStream(h.getText().getBytes(StandardCharsets.UTF_8));
                }

            };
        }

        public abstract R build();
    }

    public static abstract class ContainerBuilder<T> extends BuilderBase<ContainerBuilder<T>, T> {

        public abstract T _create(Context context);
        
        private Map<String, Object> properties= new HashMap<>();

        @Override
        public T build() {
            return _create(new Context(Serializer.INSTANCE, createService(), properties));
        }

        public ContainerBuilder<T> addProperty(String name, Object value) {
            properties.put(name, value);
            return this;
        }

    }

    public static final class Builder extends BuilderBase<Builder, HttpService> {

        @Override
        public HttpService build() {
            return createService();
        }
    }

    private static String readResource(String url, String resourceName) throws IOException, URISyntaxException {
        if (resourceName == null) {
            throw new RuntimeException("resource not found for url=" + url);
        }
        return new String(Files.readAllBytes(Paths.get(TestingService.class.getResource(resourceName).toURI())));
    }

}
