package com.github.davidmoten.microsoft.client.builder;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.microsoft.authentication.AccessTokenProvider;
import com.github.davidmoten.microsoft.authentication.AuthenticationEndpoint;
import com.github.davidmoten.microsoft.authentication.ClientCredentialsAccessTokenProvider;
import com.github.davidmoten.msgraph.builder.Authenticator;
import com.github.davidmoten.msgraph.builder.BearerAuthenticator;
import com.github.davidmoten.msgraph.builder.Creator;
import com.github.davidmoten.odata.client.Context;
import com.github.davidmoten.odata.client.HttpService;
import com.github.davidmoten.odata.client.Path;
import com.github.davidmoten.odata.client.PathStyle;
import com.github.davidmoten.odata.client.RequestHeader;
import com.github.davidmoten.odata.client.Serializer;
import com.github.davidmoten.odata.client.internal.ApacheHttpClientHttpService;

public final class MicrosoftClientBuilder<T> {

    private final Creator<T> creator;
    private final String baseUrl;
    private String tenantName;
    private String resource;
    private String scope;
    private String clientId;
    private String clientSecret;
    private long refreshBeforeExpiryDurationMs = TimeUnit.MINUTES.toMillis(5);
    private long connectTimeoutMs;
    private long readTimeoutMs;
    private Optional<String> proxyHost = Optional.empty();
    private Optional<Integer> proxyPort = Optional.empty();
    private Optional<String> proxyUsername = Optional.empty();
    private Optional<String> proxyPassword = Optional.empty();
    private Optional<Supplier<CloseableHttpClient>> httpClientSupplier = Optional.empty();
    private Optional<Function<HttpClientBuilder, HttpClientBuilder>> httpClientBuilderExtras = Optional
            .empty();
    private String authenticationEndpoint = AuthenticationEndpoint.GLOBAL.url();
    private Function<? super HttpService, ? extends HttpService> httpServiceTransformer = x -> x;
    private Optional<AccessTokenProvider> accessTokenProvider = Optional.empty();
    private Optional<Authenticator> authenticator = Optional.empty();
    private Optional<Supplier<UsernamePassword>> basicCredentials = Optional.empty();

    public MicrosoftClientBuilder(String baseUrl, Creator<T> creator) {
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
        private final MicrosoftClientBuilder<T> b;

        BuilderCustomAuthenticator(MicrosoftClientBuilder<T> b, Authenticator authenticator) {
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
                    b.proxyHost, b.proxyPort, b.proxyUsername, b.proxyPassword,
                    b.httpClientSupplier, b.httpClientBuilderExtras, b.creator,
                    b.authenticationEndpoint, b.httpServiceTransformer);
        }

    }

    public Builder5<T> basicAuthentication(Supplier<UsernamePassword> usernamePassword) {
        this.basicCredentials = Optional.of(usernamePassword);
        return new Builder5<T>(this);
    }

    public Builder<T> tenantName(String tenantName) {
        this.tenantName = tenantName;
        return new Builder<T>(this);
    }

    public static final class Builder<T> {
        private final MicrosoftClientBuilder<T> b;

        Builder(MicrosoftClientBuilder<T> b) {
            this.b = b;
        }

        public Builder2<T> resource(String resource) {
            b.resource = resource;
            return new Builder2<T>(b);
        }

    }

    public static final class Builder2<T> {
        private final MicrosoftClientBuilder<T> b;

        Builder2(MicrosoftClientBuilder<T> b) {
            this.b = b;
        }

        public Builder3<T> scope(String scope) {
            b.scope = scope;
            return new Builder3<T>(b);
        }

    }

    public static final class Builder3<T> {
        private final MicrosoftClientBuilder<T> b;

        Builder3(MicrosoftClientBuilder<T> b) {
            this.b = b;
        }

        public Builder4<T> clientId(String clientId) {
            b.clientId = clientId;
            return new Builder4<T>(b);
        }

    }

    public static final class Builder4<T> {
        private final MicrosoftClientBuilder<T> b;

        Builder4(MicrosoftClientBuilder<T> b) {
            this.b = b;
        }

        public Builder5<T> clientSecret(String clientSecret) {
            b.clientSecret = clientSecret;
            return new Builder5<T>(b);
        }

    }

    public static final class Builder5<T> {
        private final MicrosoftClientBuilder<T> b;

        Builder5(MicrosoftClientBuilder<T> b) {
            this.b = b;
        }

        public Builder5<T> refreshBeforeExpiry(long duration, TimeUnit unit) {
            b.refreshBeforeExpiryDurationMs = unit.toMillis(duration);
            return this;
        }

        public Builder5<T> connectTimeout(long duration, TimeUnit unit) {
            b.connectTimeoutMs = unit.toMillis(duration);
            return this;
        }

        public Builder5<T> readTimeout(long duration, TimeUnit unit) {
            b.readTimeoutMs = unit.toMillis(duration);
            return this;
        }

        public Builder5<T> proxyHost(String proxyHost) {
            b.proxyHost = Optional.of(proxyHost);
            return this;
        }

        public Builder5<T> proxyPort(int proxyPort) {
            b.proxyPort = Optional.of(proxyPort);
            return this;
        }

        public Builder5<T> httpServiceTransformer(
                Function<? super HttpService, ? extends HttpService> transformer) {
            b.httpServiceTransformer = transformer;
            return this;
        }

        public Builder5<T> accessTokenProvider(AccessTokenProvider atp) {
            b.accessTokenProvider = Optional.of(atp);
            return this;
        }

        public Builder5<T> proxyUsername(String username) {
            b.proxyUsername = Optional.of(username);
            return this;
        }

        public Builder5<T> proxyPassword(String password) {
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
        public Builder5<T> httpClientProvider(Supplier<CloseableHttpClient> supplier) {
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
        public Builder5<T> httpClientBuilderExtras(
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
        public Builder5<T> authenticationEndpoint(AuthenticationEndpoint authenticationEndpoint) {
            return authenticationEndpoint(authenticationEndpoint.url());
        }

        /**
         * Sets the authentication endpoint url to use for access tokens etc. If not
         * specified defaults to {@link AuthenticationEndpoint#GLOBAL} url.
         * 
         * @param authenticationEndpoint endpoint to use for authentication
         * @return this
         */
        public Builder5<T> authenticationEndpoint(String authenticationEndpoint) {
            b.authenticationEndpoint = authenticationEndpoint;
            return this;
        }

        public Builder5<T> authenticator(Authenticator authenticator) {
            b.authenticator = Optional.of(authenticator);
            return this;
        }

        public T build() {
            if (!b.authenticator.isPresent() && b.basicCredentials.isPresent()) {
                Supplier<UsernamePassword> bc = b.basicCredentials.get();
                authenticator((url, requestHeaders) -> {
                    // some streaming endpoints object to auth so don't add header
                    // if not on the base service
                    if (url.toExternalForm().startsWith(b.baseUrl)) {
                        // remove Authorization header if present
                        List<RequestHeader> list = requestHeaders //
                                .stream() //
                                .filter(rh -> !rh.name().equalsIgnoreCase("Authorization")) //
                                .collect(Collectors.toList());
                        // add basic auth request header
                        UsernamePassword c = bc.get();
                        list.add(basicAuth(c.username(), c.password()));
                        return list;
                    } else {
                        return requestHeaders;
                    }
                });
            }
            return createService(b.baseUrl, b.tenantName, b.resource, b.scope, b.clientId,
                    b.clientSecret, b.refreshBeforeExpiryDurationMs, b.connectTimeoutMs,
                    b.readTimeoutMs, b.proxyHost, b.proxyPort, b.proxyUsername, b.proxyPassword,
                    b.httpClientSupplier, b.httpClientBuilderExtras, b.creator,
                    b.authenticationEndpoint, b.httpServiceTransformer, b.accessTokenProvider,
                    b.authenticator);
        }

    }

    private static RequestHeader basicAuth(String username, String password) {
        String s = username + ":" + password;
        String encoded = Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
        return RequestHeader.create("Authorization", "Basic " + encoded);
    }

    public static final class UsernamePassword {

        private final String username;
        private final String password;

        UsernamePassword(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public static UsernamePassword create(String username, String password) {
            return new UsernamePassword(username, password);
        }

        public String username() {
            return username;
        }

        public String password() {
            return password;
        }
    }

    private static <T> T createService(String baseUrl, String tenantName, String resource,
            String scope, String clientId, String clientSecret, long refreshBeforeExpiryDurationMs,
            long connectTimeoutMs, long readTimeoutMs, //
            Optional<String> proxyHost, Optional<Integer> proxyPort, //
            Optional<String> proxyUsername, Optional<String> proxyPassword,
            Optional<Supplier<CloseableHttpClient>> supplier,
            Optional<Function<HttpClientBuilder, HttpClientBuilder>> httpClientBuilderExtras,
            Creator<T> creator, //
            String authenticationEndpoint, //
            Function<? super HttpService, ? extends HttpService> httpServiceTransformer,
            Optional<AccessTokenProvider> accessTokenProviderOverride, //
            Optional<Authenticator> authenticator) {
        final Authenticator auth;
        if (authenticator.isPresent()) {
            auth = authenticator.get();
        } else {
            AccessTokenProvider accessTokenProvider = accessTokenProviderOverride //
                    .orElseGet(() -> ClientCredentialsAccessTokenProvider //
                            .tenantName(tenantName) //
                            .resource(resource) //
                            .scope(scope) //
                            .clientId(clientId) //
                            .clientSecret(clientSecret) //
                            .connectTimeoutMs(connectTimeoutMs, TimeUnit.MILLISECONDS) //
                            .readTimeoutMs(readTimeoutMs, TimeUnit.MILLISECONDS) //
                            .refreshBeforeExpiry(refreshBeforeExpiryDurationMs,
                                    TimeUnit.MILLISECONDS) //
                            .authenticationEndpoint(authenticationEndpoint) //
                            .build());
            auth = new BearerAuthenticator(accessTokenProvider, baseUrl);
        }
        return createService(baseUrl, auth, connectTimeoutMs, readTimeoutMs, proxyHost, proxyPort,
                proxyUsername, proxyPassword, supplier, httpClientBuilderExtras, creator,
                authenticationEndpoint, httpServiceTransformer);
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

    @SuppressWarnings("resource")
    private static <T> T createService(String baseUrl, Authenticator authenticator,
            long connectTimeoutMs, long readTimeoutMs, //
            Optional<String> proxyHost, Optional<Integer> proxyPort, //
            Optional<String> proxyUsername, Optional<String> proxyPassword,
            Optional<Supplier<CloseableHttpClient>> supplier,
            Optional<Function<HttpClientBuilder, HttpClientBuilder>> httpClientBuilderExtras,
            Creator<T> creator, String authenticationEndpoint, //
            Function<? super HttpService, ? extends HttpService> httpServiceTransformer) {
        final Supplier<CloseableHttpClient> clientSupplier = createClientSupplier(connectTimeoutMs,
                readTimeoutMs, proxyHost, proxyPort, proxyUsername, proxyPassword, supplier,
                httpClientBuilderExtras);
        Path basePath = new Path(baseUrl, PathStyle.IDENTIFIERS_AS_SEGMENTS);
        HttpService httpService = new ApacheHttpClientHttpService( //
                basePath, //
                clientSupplier, //
                authenticator::authenticate);
        httpService = httpServiceTransformer.apply(httpService);
        return creator.create(new Context(Serializer.INSTANCE, httpService, createProperties()));
    }

    public static Map<String, Object> createProperties() {
        Map<String, Object> p = new HashMap<>();
        p.put("modify.stream.edit.link", "true");
        p.put("attempt.stream.when.no.metadata", "true");
        p.put("action.or.function.segment.simple.name", "true");
        return p;
    }

    private static CloseableHttpClient createHttpClient(long connectTimeoutMs, long readTimeoutMs,
            Optional<String> proxyHost, Optional<Integer> proxyPort, Optional<String> proxyUsername,
            Optional<String> proxyPassword,
            Optional<Function<HttpClientBuilder, HttpClientBuilder>> httpClientBuilderExtras) {
        RequestConfig config = RequestConfig.custom() //
                .setConnectTimeout((int) connectTimeoutMs) //
                .setSocketTimeout((int) readTimeoutMs) //
                .build();

        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        // Set soTimeout here to affect socketRead in the phase of ssl handshake. Note
        // that
        // the RequestConfig.setSocketTimeout will take effect only after the ssl
        // handshake completed.
        cm.setDefaultSocketConfig(SocketConfig.custom().setSoTimeout((int) readTimeoutMs).build());

        HttpClientBuilder b = HttpClientBuilder //
                .create() //
                .useSystemProperties() //
                .setDefaultRequestConfig(config) //
                .setConnectionManager(cm);

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
