package com.github.davidmoten.msgraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.odata.client.ClientException;
import com.github.davidmoten.odata.client.Context;
import com.github.davidmoten.odata.client.HttpService;
import com.github.davidmoten.odata.client.Path;
import com.github.davidmoten.odata.client.PathStyle;
import com.github.davidmoten.odata.client.RequestHeader;
import com.github.davidmoten.odata.client.Serializer;
import com.github.davidmoten.odata.client.internal.ApacheHttpClientHttpService;

import odata.msgraph.client.container.GraphService;

public final class MsGraph {

    private static final String MSGRAPH_1_0_BASE_URL = "https://graph.microsoft.com/v1.0";
    private static final String AUTHORIZATION_HEADER_NAME = "Authorization";
    private static final String OAUTH_BEARER_PREFIX = "Bearer ";

    private MsGraph() {
        // prevent instantiation
    }

    public static Builder tenantName(String tenantName) {
        return new Builder(tenantName);
    }

    public static final class Builder {
        String tenantName;
        String clientId;
        String clientSecret;
        long refreshBeforeExpiryDurationMs = TimeUnit.MINUTES.toMillis(5);
        long connectTimeoutMs;
        long readTimeoutMs;
        Optional<String> proxyHost = Optional.empty();
        Optional<Integer> proxyPort = Optional.empty();
        Optional<String> proxyUsername = Optional.empty();
        Optional<String> proxyPassword = Optional.empty();
        public Optional<String> proxyScheme = Optional.of("http");
        public Optional<Supplier<CloseableHttpClient>> httpClientSupplier = Optional.empty();
        public Optional<Function<HttpClientBuilder, HttpClientBuilder>> httpClientBuilderExtras = Optional.empty();

        Builder(String tenantName) {
            this.tenantName = tenantName;
        }

        public Builder2 clientId(String clientId) {
            this.clientId = clientId;
            return new Builder2(this);
        }

    }

    public static final class Builder2 {
        final Builder b;

        public Builder2(Builder b) {
            this.b = b;
        }

        public Builder3 clientSecret(String clientSecret) {
            b.clientSecret = clientSecret;
            return new Builder3(b);
        }
    }

    public static final class Builder3 {
        final Builder b;

        public Builder3(Builder b) {
            this.b = b;
        }

        public Builder3 refreshBeforeExpiry(long duration, TimeUnit unit) {
            b.refreshBeforeExpiryDurationMs = unit.toMillis(duration);
            return this;
        }

        public Builder3 connectTimeout(long duration, TimeUnit unit) {
            b.connectTimeoutMs = unit.toMillis(duration);
            return this;
        }

        public Builder3 readTimeout(long duration, TimeUnit unit) {
            b.readTimeoutMs = unit.toMillis(duration);
            return this;
        }

        public Builder3 proxyScheme(String proxyScheme) {
            b.proxyScheme = Optional.of(proxyScheme);
            return this;
        }

        public Builder3 proxyHost(String proxyHost) {
            b.proxyHost = Optional.of(proxyHost);
            return this;
        }

        public Builder3 proxyPort(int proxyPort) {
            b.proxyPort = Optional.of(proxyPort);
            return this;
        }

        public Builder3 proxyUsername(String username) {
            b.proxyUsername = Optional.of(username);
            return this;
        }

        public Builder3 proxyPassword(String password) {
            b.proxyPassword = Optional.of(password);
            return this;
        }

        /**
         * Do your own thing to create an Apache {@link HttpClient}. This method might
         * disappear if the underlying http service gets swapped out for another one.
         * You might want to use this method if your proxy interaction is complicated
         * for example.
         * 
         * @param supplier
         *            provider of HttpClient
         * @return this
         */
        public Builder3 httpClientProvider(Supplier<CloseableHttpClient> supplier) {
            Preconditions.checkArgument(!b.httpClientBuilderExtras.isPresent());
            b.httpClientSupplier = Optional.of(supplier);
            return this;
        }

        /**
         * Do your own thing to further modify a configured {@link HttpClientBuilder}.
         * This method might disappear if the underlying http service gets swapped out
         * for another one. You might want to use this method if your proxy interaction
         * is complicated for example or if you want to use interceptors or other fancy
         * stuff.
         * 
         * @param extras
         *            modifier of builder
         * @return this
         */
        public Builder3 httpClientBuilderExtras(Function<HttpClientBuilder, HttpClientBuilder> extras) {
            Preconditions.checkArgument(!b.httpClientSupplier.isPresent());
            b.httpClientBuilderExtras = Optional.of(extras);
            return this;
        }

        public GraphService build() {
            return createService(b.tenantName, b.clientId, b.clientSecret, b.refreshBeforeExpiryDurationMs,
                    b.connectTimeoutMs, b.readTimeoutMs, b.proxyHost, b.proxyPort, b.proxyScheme, b.proxyUsername,
                    b.proxyPassword, b.httpClientSupplier, b.httpClientBuilderExtras);
        }
    }

    private static GraphService createService(String tenantName, String clientId, String clientSecret,
            long refreshBeforeExpiryDurationMs, long connectTimeoutMs, long readTimeoutMs, //
            Optional<String> proxyHost, Optional<Integer> proxyPort, Optional<String> proxyScheme, //
            Optional<String> proxyUsername, Optional<String> proxyPassword,
            Optional<Supplier<CloseableHttpClient>> supplier,
            Optional<Function<HttpClientBuilder, HttpClientBuilder>> httpClientBuilderExtras) {
        MsGraphAccessTokenProvider accessTokenProvider = MsGraphAccessTokenProvider //
                .tenantName(tenantName) //
                .clientId(clientId) //
                .clientSecret(clientSecret) //
                .connectTimeoutMs(connectTimeoutMs, TimeUnit.MILLISECONDS) //
                .readTimeoutMs(readTimeoutMs, TimeUnit.MILLISECONDS) //
                .refreshBeforeExpiry(refreshBeforeExpiryDurationMs, TimeUnit.MILLISECONDS) //
                .build();
        Path basePath = new Path(MSGRAPH_1_0_BASE_URL, PathStyle.IDENTIFIERS_AS_SEGMENTS);
        final Supplier<CloseableHttpClient> clientSupplier;
        if (supplier.isPresent()) {
            clientSupplier = supplier.get();
        } else {
            clientSupplier = () -> {
                RequestConfig config = RequestConfig.custom() //
                        .setConnectTimeout((int) connectTimeoutMs) //
                        .setSocketTimeout((int) readTimeoutMs) //
                        .build();
                HttpClientBuilder b = HttpClientBuilder //
                        .create() //
                        .useSystemProperties() //
                        .setDefaultRequestConfig(config);
                if (proxyHost.isPresent()) {
                    HttpHost proxy = new HttpHost(proxyHost.get(), proxyPort.get());
                    if (proxyUsername.isPresent()) {
                        Credentials credentials = new UsernamePasswordCredentials(proxyUsername.get(),
                                proxyPassword.orElse(null));
                        AuthScope authScope = new AuthScope(proxyHost.get(), proxyPort.get());
                        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                        credentialsProvider.setCredentials(authScope, credentials);
                        b = b.setDefaultCredentialsProvider(credentialsProvider);
                    }
                    b = b.setProxy(proxy);
                }
                if (httpClientBuilderExtras.isPresent()) {
                    b = httpClientBuilderExtras.get().apply(b);
                }
                return b.build();
            };
        }
        HttpService httpService = new ApacheHttpClientHttpService( //
                basePath, //
                clientSupplier, //
                m -> authenticate(m, accessTokenProvider));
        Map<String, Object> properties = new HashMap<>();
        properties.put("modify.stream.edit.link", "true");
        return new GraphService(new Context(Serializer.INSTANCE, httpService, properties));
    }

    public static List<RequestHeader> authenticate(List<RequestHeader> m,
            MsGraphAccessTokenProvider accessTokenProvider) {
        if (m.stream().anyMatch(x -> x.name().equals(AUTHORIZATION_HEADER_NAME))) {
            return m;
        } else {
            List<RequestHeader> m2 = new ArrayList<>(m);
            try {
                final String token = accessTokenProvider.get();
                m2.add(new RequestHeader(AUTHORIZATION_HEADER_NAME, OAUTH_BEARER_PREFIX + token));
            } catch (Throwable e) {
                final String message = "Unable to authenticate request";
                throw new ClientException(message, e);
            }
            return m2;
        }
    }
}
