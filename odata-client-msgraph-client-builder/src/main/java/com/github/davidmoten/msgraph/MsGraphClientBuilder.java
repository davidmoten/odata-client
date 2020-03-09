package com.github.davidmoten.msgraph;

import java.util.HashMap;
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
import com.github.davidmoten.odata.client.Context;
import com.github.davidmoten.odata.client.HttpService;
import com.github.davidmoten.odata.client.Path;
import com.github.davidmoten.odata.client.PathStyle;
import com.github.davidmoten.odata.client.Serializer;
import com.github.davidmoten.odata.client.internal.ApacheHttpClientHttpService;

public final class MsGraphClientBuilder<T> {

    private final Creator<T> creator;
    private final String baseUrl;
    private String tenantName;
    private String clientId;
    private String clientSecret;
    private long refreshBeforeExpiryDurationMs = TimeUnit.MINUTES.toMillis(5);
    private long connectTimeoutMs;
    private long readTimeoutMs;
    private Optional<String> proxyHost = Optional.empty();
    private Optional<Integer> proxyPort = Optional.empty();
    private Optional<String> proxyUsername = Optional.empty();
    private Optional<String> proxyPassword = Optional.empty();
    private Optional<String> proxyScheme = Optional.of("http");
    private Optional<Supplier<CloseableHttpClient>> httpClientSupplier = Optional.empty();
    private Optional<Function<HttpClientBuilder, HttpClientBuilder>> httpClientBuilderExtras = Optional
            .empty();
    private String authenticationEndpoint = AuthenticationEndpoint.GLOBAL.url();
    public Authenticator authenticator;

    public MsGraphClientBuilder(String baseUrl, Creator<T> creator) {
        Preconditions.checkNotNull(baseUrl);
        Preconditions.checkNotNull(creator);
        this.baseUrl = baseUrl;
        this.creator = creator;
    }

    public BuilderCustomAuthenticator<T> authenticator(Authenticator authenticator) {
        return new BuilderCustomAuthenticator<T>(this, authenticator);
    }

    public static final class BuilderCustomAuthenticator<T> {

        private final Authenticator authenticator;
        private MsGraphClientBuilder<T> b;

        BuilderCustomAuthenticator(MsGraphClientBuilder<T> b, Authenticator authenticator) {
            this.authenticator = authenticator;
            this.b = b;
        }

        public BuilderCustomAuthenticator<T> connectTimeout(long duration, TimeUnit unit) {
            b.connectTimeoutMs = unit.toMillis(duration);
            return this;
        }

        public BuilderCustomAuthenticator<T> readTimeout(long duration, TimeUnit unit) {
            b.readTimeoutMs = unit.toMillis(duration);
            return this;
        }

        public BuilderCustomAuthenticator<T> proxyScheme(String proxyScheme) {
            b.proxyScheme = Optional.of(proxyScheme);
            return this;
        }

        public BuilderCustomAuthenticator<T> proxyHost(String proxyHost) {
            b.proxyHost = Optional.of(proxyHost);
            return this;
        }

        public BuilderCustomAuthenticator<T> proxyPort(int proxyPort) {
            b.proxyPort = Optional.of(proxyPort);
            return this;
        }

        public BuilderCustomAuthenticator<T> proxyUsername(String username) {
            b.proxyUsername = Optional.of(username);
            return this;
        }

        public BuilderCustomAuthenticator<T> proxyPassword(String password) {
            b.proxyPassword = Optional.of(password);
            return this;
        }

        /**
         * Do your own thing to create an Apache {@link HttpClient}. This method might
         * disappear if the underlying http service gets swapped out for another one.
         * You might want to use this method if your proxy interaction is complicated
         * for example.
         * 
         * @param supplier provider of HttpClient
         * @return this
         */
        public BuilderCustomAuthenticator<T> httpClientProvider(
                Supplier<CloseableHttpClient> supplier) {
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
         * @param extras modifier of builder
         * @return this
         */
        public BuilderCustomAuthenticator<T> httpClientBuilderExtras(
                Function<HttpClientBuilder, HttpClientBuilder> extras) {
            Preconditions.checkArgument(!b.httpClientSupplier.isPresent());
            b.httpClientBuilderExtras = Optional.of(extras);
            return this;
        }

        public T build() {
            return createService(b.baseUrl, authenticator, b.connectTimeoutMs, b.readTimeoutMs,
                    b.proxyHost, b.proxyPort, b.proxyScheme, b.proxyUsername, b.proxyPassword,
                    b.httpClientSupplier, b.httpClientBuilderExtras, b.creator,
                    b.authenticationEndpoint);
        }

    }

    public Builder<T> tenantName(String tenantName) {
        this.tenantName = tenantName;
        return new Builder<T>(this);
    }

    public static final class Builder<T> {
        private final MsGraphClientBuilder<T> b;

        Builder(MsGraphClientBuilder<T> b) {
            this.b = b;
        }

        public Builder2<T> clientId(String clientId) {
            b.clientId = clientId;
            return new Builder2<T>(b);
        }

    }

    public static final class Builder2<T> {
        private final MsGraphClientBuilder<T> b;

        Builder2(MsGraphClientBuilder<T> b) {
            this.b = b;
        }

        public Builder3<T> clientSecret(String clientSecret) {
            b.clientSecret = clientSecret;
            return new Builder3<T>(b);
        }

    }

    public static final class Builder3<T> {
        private final MsGraphClientBuilder<T> b;

        Builder3(MsGraphClientBuilder<T> b) {
            this.b = b;
        }

        public Builder3<T> refreshBeforeExpiry(long duration, TimeUnit unit) {
            b.refreshBeforeExpiryDurationMs = unit.toMillis(duration);
            return this;
        }

        public Builder3<T> connectTimeout(long duration, TimeUnit unit) {
            b.connectTimeoutMs = unit.toMillis(duration);
            return this;
        }

        public Builder3<T> readTimeout(long duration, TimeUnit unit) {
            b.readTimeoutMs = unit.toMillis(duration);
            return this;
        }

        public Builder3<T> proxyScheme(String proxyScheme) {
            b.proxyScheme = Optional.of(proxyScheme);
            return this;
        }

        public Builder3<T> proxyHost(String proxyHost) {
            b.proxyHost = Optional.of(proxyHost);
            return this;
        }

        public Builder3<T> proxyPort(int proxyPort) {
            b.proxyPort = Optional.of(proxyPort);
            return this;
        }

        public Builder3<T> proxyUsername(String username) {
            b.proxyUsername = Optional.of(username);
            return this;
        }

        public Builder3<T> proxyPassword(String password) {
            b.proxyPassword = Optional.of(password);
            return this;
        }

        /**
         * Do your own thing to create an Apache {@link HttpClient}. This method might
         * disappear if the underlying http service gets swapped out for another one.
         * You might want to use this method if your proxy interaction is complicated
         * for example.
         * 
         * @param supplier provider of HttpClient
         * @return this
         */
        public Builder3<T> httpClientProvider(Supplier<CloseableHttpClient> supplier) {
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
         * @param extras modifier of builder
         * @return this
         */
        public Builder3<T> httpClientBuilderExtras(
                Function<HttpClientBuilder, HttpClientBuilder> extras) {
            Preconditions.checkArgument(!b.httpClientSupplier.isPresent());
            b.httpClientBuilderExtras = Optional.of(extras);
            return this;
        }

        /**
         * Sets the authentication endpoint url to use for access tokens etc. If not
         * specified defaults to {@link AuthenticationEndpoint#GLOBAL}.
         * 
         * @param authenticationEndpoint endpoint to use for authentication
         * @return this
         */
        public Builder3<T> authenticationEndpoint(AuthenticationEndpoint authenticationEndpoint) {
            return authenticationEndpoint(authenticationEndpoint.url());
        }

        /**
         * Sets the authentication endpoint url to use for access tokens etc. If not
         * specified defaults to {@link AuthenticationEndpoint#GLOBAL} url.
         * 
         * @param authenticationEndpoint endpoint to use for authentication
         * @return this
         */
        public Builder3<T> authenticationEndpoint(String authenticationEndpoint) {
            b.authenticationEndpoint = authenticationEndpoint;
            return this;
        }

        public T build() {
            return createService(b.baseUrl, b.tenantName, b.clientId, b.clientSecret,
                    b.refreshBeforeExpiryDurationMs, b.connectTimeoutMs, b.readTimeoutMs,
                    b.proxyHost, b.proxyPort, b.proxyScheme, b.proxyUsername, b.proxyPassword,
                    b.httpClientSupplier, b.httpClientBuilderExtras, b.creator,
                    b.authenticationEndpoint);
        }

    }

    private static <T> T createService(String baseUrl, String tenantName, String clientId,
            String clientSecret, long refreshBeforeExpiryDurationMs, long connectTimeoutMs,
            long readTimeoutMs, //
            Optional<String> proxyHost, Optional<Integer> proxyPort, Optional<String> proxyScheme, //
            Optional<String> proxyUsername, Optional<String> proxyPassword,
            Optional<Supplier<CloseableHttpClient>> supplier,
            Optional<Function<HttpClientBuilder, HttpClientBuilder>> httpClientBuilderExtras,
            Creator<T> creator, String authenticationEndpoint) {
        ClientCredentialsAccessTokenProvider accessTokenProvider = ClientCredentialsAccessTokenProvider //
                .tenantName(tenantName) //
                .clientId(clientId) //
                .clientSecret(clientSecret) //
                .connectTimeoutMs(connectTimeoutMs, TimeUnit.MILLISECONDS) //
                .readTimeoutMs(readTimeoutMs, TimeUnit.MILLISECONDS) //
                .refreshBeforeExpiry(refreshBeforeExpiryDurationMs, TimeUnit.MILLISECONDS) //
                .authenticationEndpoint(authenticationEndpoint) //
                .build();

        Authenticator authenticator = new BearerAuthenticator(accessTokenProvider);

        return createService(baseUrl, authenticator, connectTimeoutMs, readTimeoutMs, proxyHost,
                proxyPort, proxyScheme, proxyUsername, proxyPassword, supplier,
                httpClientBuilderExtras, creator, authenticationEndpoint);
    }

    private static Supplier<CloseableHttpClient> createClientSupplier(long connectTimeoutMs,
            long readTimeoutMs, Optional<String> proxyHost, Optional<Integer> proxyPort,
            Optional<String> proxyUsername, Optional<String> proxyPassword,
            Optional<Supplier<CloseableHttpClient>> supplier,
            Optional<Function<HttpClientBuilder, HttpClientBuilder>> httpClientBuilderExtras) {
        final Supplier<CloseableHttpClient> clientSupplier;
        if (supplier.isPresent()) {
            clientSupplier = supplier.get();
        } else {
            clientSupplier = () -> createHttpClient(connectTimeoutMs, readTimeoutMs, proxyHost,
                    proxyPort, proxyUsername, proxyPassword, httpClientBuilderExtras);
        }
        return clientSupplier;
    }

    private static <T> T createService(String baseUrl, Authenticator authenticator,
            long connectTimeoutMs, long readTimeoutMs, //
            Optional<String> proxyHost, Optional<Integer> proxyPort, Optional<String> proxyScheme, //
            Optional<String> proxyUsername, Optional<String> proxyPassword,
            Optional<Supplier<CloseableHttpClient>> supplier,
            Optional<Function<HttpClientBuilder, HttpClientBuilder>> httpClientBuilderExtras,
            Creator<T> creator, String authenticationEndpoint) {
        final Supplier<CloseableHttpClient> clientSupplier = createClientSupplier(connectTimeoutMs,
                readTimeoutMs, proxyHost, proxyPort, proxyUsername, proxyPassword, supplier,
                httpClientBuilderExtras);
        Path basePath = new Path(baseUrl, PathStyle.IDENTIFIERS_AS_SEGMENTS);
        HttpService httpService = new ApacheHttpClientHttpService( //
                basePath, //
                clientSupplier, //
                authenticator::authenticate);
        Map<String, Object> properties = new HashMap<>();
        properties.put("modify.stream.edit.link", "true");
        properties.put("attempt.stream.when.no.metadata", "true");
        return creator.create(new Context(Serializer.INSTANCE, httpService, properties));
    }

    private static CloseableHttpClient createHttpClient(long connectTimeoutMs, long readTimeoutMs,
            Optional<String> proxyHost, Optional<Integer> proxyPort, Optional<String> proxyUsername,
            Optional<String> proxyPassword,
            Optional<Function<HttpClientBuilder, HttpClientBuilder>> httpClientBuilderExtras) {
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
    }

}
