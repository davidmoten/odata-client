package com.github.davidmoten.odata.client;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class TestingService {

    public static Builder replyWithResource(String url, String resourceName) {
        return new Builder().replyWithResource(url, resourceName);
    }

    public static Builder baseUrl(String url) {
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

        public Builder replyWithResource(String url, String resourceName) {
            responses.put(url, resourceName);
            return this;
        }

        public Service build() {
            return new Service() {

                @Override
                public ResponseGet GET(String url, Map<String, String> requestHeaders) {
                    String resourceName = responses.get(url);
                    if (resourceName == null) {
                        throw new RuntimeException("response not found for url=" + url);
                    }
                    try {
                        String text = new String(
                                Files.readAllBytes(Paths.get(TestingService.class.getResource(resourceName).toURI())));
                        return new ResponseGet(200, text);
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
