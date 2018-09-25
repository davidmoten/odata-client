package com.github.davidmoten.odata.client;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class TestingService {

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

    public static final class Builder {
        Map<String, String> responses = new HashMap<>();
        String baseUrl = "https://testing.com";
        private PathStyle pathStyle = PathStyle.IDENTIFIERS_AS_SEGMENTS;

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder pathStyle(PathStyle pathStyle) {
            this.pathStyle = pathStyle;
            return this;
        }

        public Builder replyWithResource(String path, String resourceName) {
            responses.put(toKey(HttpMethod.GET, baseUrl + path), resourceName);
            return this;
        }

        public Builder replyWithResource(String path, String resourceName, HttpMethod method) {
            responses.put(toKey(method, baseUrl + path), resourceName);
            return this;
        }

        private static String toKey(HttpMethod method, String url) {
            return method + "_" + url;
        }

        public Service build() {
            return new Service() {

                @Override
                public HttpResponse GET(String url, Map<String, String> requestHeaders) {
                    String resourceName = responses.get(toKey(HttpMethod.GET, url));
                    if (resourceName == null) {
                        throw new RuntimeException("GET response not found for url=" + url);
                    }
                    try {
                        URL resource = TestingService.class.getResource(resourceName);
                        if (resource == null) {
                            throw new RuntimeException("resource not found on classpath: " + resourceName);
                        }
                        String text = new String(
                                Files.readAllBytes(Paths.get(resource.toURI())));
                        return new HttpResponse(200, text);
                    } catch (IOException | URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public HttpResponse PATCH(String url, Map<String, String> requestHeaders) {
                    String resourceName = responses.get(toKey(HttpMethod.PATCH, url));
                    if (resourceName == null) {
                        throw new RuntimeException("PATCH response not found for url=" + url);
                    }
                    try {
                        String text = new String(
                                Files.readAllBytes(Paths.get(TestingService.class.getResource(resourceName).toURI())));
                        return new HttpResponse(200, text);
                    } catch (IOException | URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public Path getBasePath() {
                    return new Path(baseUrl, pathStyle);
                }
            };

        }
    }

}
