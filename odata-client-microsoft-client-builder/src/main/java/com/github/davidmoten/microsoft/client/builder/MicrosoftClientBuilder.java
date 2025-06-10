package com.github.davidmoten.microsoft.client.builder;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.github.davidmoten.microsoft.authentication.Authenticator;
import com.github.davidmoten.microsoft.authentication.BearerAuthenticator;
import com.github.davidmoten.microsoft.authentication.ClientCredentialsAccessTokenProvider;
import com.github.davidmoten.odata.client.Context;
import com.github.davidmoten.odata.client.HttpService;
import com.github.davidmoten.odata.client.Path;
import com.github.davidmoten.odata.client.PathStyle;
import com.github.davidmoten.odata.client.Properties;
import com.github.davidmoten.odata.client.RequestHeader;
import com.github.davidmoten.odata.client.SchemaInfo;
import com.github.davidmoten.odata.client.Serializer;
import com.github.davidmoten.odata.client.internal.ApacheHttpClientHttpService;

public final class MicrosoftClientBuilder<T> {

    private static final String OAUTH2_TOKEN_URL_SUFFIX = "/oauth2/token";

    private final Creator<T> creator;
    private final String baseUrl;
    private Optional<String> tokenUrl = Optional.empty();
    private Optional<TenantNameAndEndpoint> tenantNameAndEndpoint = Optional.empty();
    private String resource;
    private final List<String> scopes = new ArrayList<>();
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
    private Optional<Function<HttpClientBuilder, HttpClientBuilder>> httpClientBuilderExtras = Optional.empty();
    private Function<? super HttpService, ? extends HttpService> httpServiceTransformer = x -> x;
    private Optional<AccessTokenProvider> accessTokenProvider = Optional.empty();
    private Optional<Authenticator> authenticator = Optional.empty();
    private Optional<Supplier<UsernamePassword>> basicCredentials = Optional.empty();
    private final List<SchemaInfo> schemas;
    private final PathStyle pathStyle;

    MicrosoftClientBuilder(String baseUrl, Creator<T> creator, List<SchemaInfo> schemas, PathStyle pathStyle) {
        Preconditions.checkNotNull(baseUrl);
        Preconditions.checkNotNull(creator);
        this.baseUrl = baseUrl;
        this.creator = creator;
        this.schemas = schemas;
        this.pathStyle = pathStyle;
    }

    public static BuilderWithBaseUrl baseUrl(String baseUrl) {
        Preconditions.checkNotNull(baseUrl);
        return new BuilderWithBaseUrl(baseUrl);
    }

    public static final class BuilderWithBaseUrl {

        private final String baseUrl;

        BuilderWithBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public <T> BuilderWithCreator<T> creator(Creator<T> creator) {
            return new BuilderWithCreator<T>(this, creator);
        }

    }

    public static final class BuilderWithCreator<T> {

        private final BuilderWithBaseUrl b;
        private final Creator<T> creator;
        private final List<SchemaInfo> schemas = new ArrayList<>();
        private PathStyle pathStyle = PathStyle.IDENTIFIERS_AS_SEGMENTS;

        BuilderWithCreator(BuilderWithBaseUrl b, Creator<T> creator) {
            this.b = b;
            this.creator = creator;
        }

        public BuilderWithCreator<T> addSchema(SchemaInfo schema) {
            schemas.add(schema);
            return this;
        }

        /**
         * Sets the path style. Default is {@link PathStyle#IDENTIFIERS_AS_SEGMENTS}.
         * 
         * @param pathStyle path style to use
         * @return this
         */
        public BuilderWithCreator<T> pathStyle(PathStyle pathStyle) {
            this.pathStyle = pathStyle;
            return this;
        }

        public MicrosoftClientBuilder<T> build() {
            return new MicrosoftClientBuilder<T>(b.baseUrl, creator, schemas, pathStyle);
        }

    }

    public BuilderWithCustomAuthenticator<T> authenticator(Authenticator authenticator) {
        return new BuilderWithCustomAuthenticator<T>(this, authenticator);
    }

    public static final class BuilderWithCustomAuthenticator<T> {

        private final Authenticator authenticator;
        private final MicrosoftClientBuilder<T> b;

        BuilderWithCustomAuthenticator(MicrosoftClientBuilder<T> b, Authenticator authenticator) {
            this.authenticator = authenticator;
            this.b = b;
        }

        public BuilderWithCustomAuthenticator<T> connectTimeout(long duration, TimeUnit unit) {
            b.connectTimeoutMs = unit.toMillis(duration);
            return this;
        }

        public BuilderWithCustomAuthenticator<T> readTimeout(long duration, TimeUnit unit) {
            b.readTimeoutMs = unit.toMillis(duration);
            return this;
        }

        public BuilderWithCustomAuthenticator<T> proxyHost(String proxyHost) {
            b.proxyHost = Optional.of(proxyHost);
            return this;
        }

        public BuilderWithCustomAuthenticator<T> proxyPort(int proxyPort) {
            b.proxyPort = Optional.of(proxyPort);
            return this;
        }

        public BuilderWithCustomAuthenticator<T> proxyUsername(String username) {
            b.proxyUsername = Optional.of(username);
            return this;
        }

        public BuilderWithCustomAuthenticator<T> proxyPassword(String password) {
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
        public BuilderWithCustomAuthenticator<T> httpClientProvider(Supplier<CloseableHttpClient> supplier) {
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
        public BuilderWithCustomAuthenticator<T> httpClientBuilderExtras(
                Function<HttpClientBuilder, HttpClientBuilder> extras) {
            Preconditions.checkArgument(!b.httpClientSupplier.isPresent());
            b.httpClientBuilderExtras = Optional.of(extras);
            return this;
        }

        public T build() {
            return createService(b.baseUrl, authenticator, b.connectTimeoutMs, b.readTimeoutMs, b.proxyHost,
                    b.proxyPort, b.proxyUsername, b.proxyPassword, b.httpClientSupplier, b.httpClientBuilderExtras,
                    b.creator, b.httpServiceTransformer, b.schemas, b.pathStyle);
        }

    }

    public BuilderWithBasicAuthentication<T> basicAuthentication(Supplier<UsernamePassword> usernamePassword) {
        this.basicCredentials = Optional.of(usernamePassword);
        return new BuilderWithBasicAuthentication<T>(this);
    }
    
    public Builder<T> tokenUrl(String tokenUrl) {
        this.tokenUrl = Optional.of(tokenUrl);
        return new Builder<T>(this);
    }

    public Builder<T> tenantName(String tenantName) {
        if (!tenantNameAndEndpoint.isPresent()) {
            this.tenantNameAndEndpoint = Optional.of(new TenantNameAndEndpoint(tenantName, AuthenticationEndpoint.GLOBAL.url()));
        } else {
            this.tenantNameAndEndpoint = Optional.of(new TenantNameAndEndpoint(tenantName, tenantNameAndEndpoint.get().authenticationEndpoint()));
        }
        return new Builder<T>(this);
    }

    public static final class Builder<T> {
        private final MicrosoftClientBuilder<T> b;

        Builder(MicrosoftClientBuilder<T> b) {
            this.b = b;
        }

        public BuilderWithResource<T> resource(String resource) {
            Preconditions.checkNotNull(resource);
            b.resource = resource;
            return new BuilderWithResource<T>(b);
        }

    }

    public static final class BuilderWithResource<T> {
        private final MicrosoftClientBuilder<T> b;

        BuilderWithResource(MicrosoftClientBuilder<T> b) {
            this.b = b;
        }

        public BuilderWithScopes<T> scope(String... scopes) {
            Preconditions.checkNotNull(scopes);
            return scope(Arrays.asList(scopes));
        }

        public BuilderWithScopes<T> scope(List<String> scopes) {
            Preconditions.checkNotNull(scopes);
            b.scopes.addAll(scopes);
            return new BuilderWithScopes<T>(b);
        }

        public BuilderWithClientId<T> clientId(String clientId) {
            return new BuilderWithScopes<T>(b).clientId(clientId);
        }

    }

    public static final class BuilderWithScopes<T> {
        private final MicrosoftClientBuilder<T> b;

        BuilderWithScopes(MicrosoftClientBuilder<T> b) {
            this.b = b;
        }

        public BuilderWithClientId<T> clientId(String clientId) {
            b.clientId = clientId;
            return new BuilderWithClientId<T>(b);
        }

    }

    public static final class BuilderWithClientId<T> {
        private final MicrosoftClientBuilder<T> b;

        BuilderWithClientId(MicrosoftClientBuilder<T> b) {
            this.b = b;
        }

        public BuilderWithBasicAuthentication<T> clientSecret(String clientSecret) {
            b.clientSecret = clientSecret;
            return new BuilderWithBasicAuthentication<T>(b);
        }

    }

    public static final class BuilderWithBasicAuthentication<T> {
        private final MicrosoftClientBuilder<T> b;

        BuilderWithBasicAuthentication(MicrosoftClientBuilder<T> b) {
            this.b = b;
        }

        public BuilderWithBasicAuthentication<T> refreshBeforeExpiry(long duration, TimeUnit unit) {
            b.refreshBeforeExpiryDurationMs = unit.toMillis(duration);
            return this;
        }

        public BuilderWithBasicAuthentication<T> connectTimeout(long duration, TimeUnit unit) {
            b.connectTimeoutMs = unit.toMillis(duration);
            return this;
        }

        public BuilderWithBasicAuthentication<T> readTimeout(long duration, TimeUnit unit) {
            b.readTimeoutMs = unit.toMillis(duration);
            return this;
        }

        public BuilderWithBasicAuthentication<T> proxyHost(String proxyHost) {
            b.proxyHost = Optional.of(proxyHost);
            return this;
        }

        public BuilderWithBasicAuthentication<T> proxyPort(int proxyPort) {
            b.proxyPort = Optional.of(proxyPort);
            return this;
        }

        public BuilderWithBasicAuthentication<T> httpServiceTransformer(Function<? super HttpService, ? extends HttpService> transformer) {
            b.httpServiceTransformer = transformer;
            return this;
        }

        public BuilderWithBasicAuthentication<T> accessTokenProvider(AccessTokenProvider atp) {
            b.accessTokenProvider = Optional.of(atp);
            return this;
        }

        public BuilderWithBasicAuthentication<T> proxyUsername(String username) {
            b.proxyUsername = Optional.of(username);
            return this;
        }

        public BuilderWithBasicAuthentication<T> proxyPassword(String password) {
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
        public BuilderWithBasicAuthentication<T> httpClientProvider(Supplier<CloseableHttpClient> supplier) {
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
        public BuilderWithBasicAuthentication<T> httpClientBuilderExtras(Function<HttpClientBuilder, HttpClientBuilder> extras) {
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
        public BuilderWithBasicAuthentication<T> authenticationEndpoint(AuthenticationEndpoint authenticationEndpoint) {
            return authenticationEndpoint(authenticationEndpoint.url());
        }

        /**
         * Sets the authentication endpoint url to use for access tokens etc. If not
         * specified defaults to {@link AuthenticationEndpoint#GLOBAL} url.
         * 
         * @param authenticationEndpoint endpoint to use for authentication
         * @return this
         */
        public BuilderWithBasicAuthentication<T> authenticationEndpoint(String authenticationEndpoint) {
            if (b.tenantNameAndEndpoint.isPresent()) {
                b.tenantNameAndEndpoint = Optional.of(
                        new TenantNameAndEndpoint(b.tenantNameAndEndpoint.get().tenantName(), authenticationEndpoint));
            } else {
                throw new IllegalArgumentException("must set tenantName to set authenticationEndpoint (cannot set tokenUrl)");
            }
            return this;
        }
        
        public BuilderWithBasicAuthentication<T> authenticator(Authenticator authenticator) {
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
            return createService(b.baseUrl, b.tokenUrl, b.tenantNameAndEndpoint, b.resource, b.scopes, b.clientId, b.clientSecret,
                    b.refreshBeforeExpiryDurationMs, b.connectTimeoutMs, b.readTimeoutMs, b.proxyHost, b.proxyPort,
                    b.proxyUsername, b.proxyPassword, b.httpClientSupplier, b.httpClientBuilderExtras, b.creator,
                    b.httpServiceTransformer, b.accessTokenProvider, b.authenticator,
                    b.schemas, b.pathStyle);
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

    private static <T> T createService(String baseUrl, Optional<String> tokenUrl,
            Optional<TenantNameAndEndpoint> tenantNameAndEndpoints, String resource, List<String> scopes,
            String clientId, String clientSecret, long refreshBeforeExpiryDurationMs, long connectTimeoutMs,
            long readTimeoutMs, //
            Optional<String> proxyHost, Optional<Integer> proxyPort, //
            Optional<String> proxyUsername, Optional<String> proxyPassword,
            Optional<Supplier<CloseableHttpClient>> supplier,
            Optional<Function<HttpClientBuilder, HttpClientBuilder>> httpClientBuilderExtras, Creator<T> creator, //
            Function<? super HttpService, ? extends HttpService> httpServiceTransformer,
            Optional<AccessTokenProvider> accessTokenProviderOverride, //
            Optional<Authenticator> authenticator, List<SchemaInfo> schemas, PathStyle pathStyle) {
        final Authenticator auth;
        if (authenticator.isPresent()) {
            auth = authenticator.get();
        } else {
            String url = tokenUrl.orElseGet(() -> tenantNameAndEndpoints
                    .map(x -> x.authenticationEndpoint() + x.tenantName() + OAUTH2_TOKEN_URL_SUFFIX)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "must provide one of tokenUrl or tenantNameAndEndpoints")));
            AccessTokenProvider accessTokenProvider = accessTokenProviderOverride //
                    .orElseGet(() -> ClientCredentialsAccessTokenProvider //
                            .url(url) //
                            .resource(resource) //
                            .scope(scopes) //
                            .clientId(clientId) //
                            .clientSecret(clientSecret) //
                            .connectTimeoutMs(connectTimeoutMs, TimeUnit.MILLISECONDS) //
                            .readTimeoutMs(readTimeoutMs, TimeUnit.MILLISECONDS) //
                            .refreshBeforeExpiry(refreshBeforeExpiryDurationMs, TimeUnit.MILLISECONDS) //
                            .proxyHost(proxyHost) //
                            .proxyPort(proxyPort) //
                            .proxyUsername(proxyUsername) //
                            .proxyPassword(proxyPassword) //
                            .build());
            auth = new BearerAuthenticator(accessTokenProvider, baseUrl);
        }
        return createService(baseUrl, auth, connectTimeoutMs, readTimeoutMs, proxyHost, proxyPort, proxyUsername,
                proxyPassword, supplier, httpClientBuilderExtras, creator, 
                httpServiceTransformer, schemas, pathStyle);
    }

    private static Supplier<CloseableHttpClient> createClientSupplier(long connectTimeoutMs, long readTimeoutMs,
            Optional<String> proxyHost, Optional<Integer> proxyPort, Optional<String> proxyUsername,
            Optional<String> proxyPassword, Optional<Supplier<CloseableHttpClient>> supplier,
            Optional<Function<HttpClientBuilder, HttpClientBuilder>> httpClientBuilderExtras) {
        final Supplier<CloseableHttpClient> clientSupplier;
        if (supplier.isPresent()) {
            clientSupplier = supplier.get();
        } else {
            clientSupplier = () -> createHttpClient(connectTimeoutMs, readTimeoutMs, proxyHost, proxyPort,
                    proxyUsername, proxyPassword, httpClientBuilderExtras);
        }
        return clientSupplier;
    }

    private static <T> T createService(String baseUrl, Authenticator authenticator, long connectTimeoutMs,
            long readTimeoutMs, //
            Optional<String> proxyHost, Optional<Integer> proxyPort, //
            Optional<String> proxyUsername, Optional<String> proxyPassword,
            Optional<Supplier<CloseableHttpClient>> supplier,
            Optional<Function<HttpClientBuilder, HttpClientBuilder>> httpClientBuilderExtras, Creator<T> creator,
            Function<? super HttpService, ? extends HttpService> httpServiceTransformer, //
            List<SchemaInfo> schemas, PathStyle pathStyle) {
        final Supplier<CloseableHttpClient> clientSupplier = createClientSupplier(connectTimeoutMs, readTimeoutMs,
                proxyHost, proxyPort, proxyUsername, proxyPassword, supplier, httpClientBuilderExtras);
        Path basePath = new Path(baseUrl, pathStyle);
        HttpService httpService = new ApacheHttpClientHttpService( //
                basePath, //
                clientSupplier, //
                authenticator::authenticate);
        httpService = httpServiceTransformer.apply(httpService);
        return creator.create(new Context(Serializer.INSTANCE, httpService, createProperties(), schemas));
    }

    public static Map<String, Object> createProperties() {
        Map<String, Object> p = new HashMap<>();
        p.put(Properties.MODIFY_STREAM_EDIT_LINK, "true");
        p.put(Properties.ATTEMPT_STREAM_WHEN_NO_METADATA, "true");
        p.put(Properties.ACTION_OR_FUNCTION_SEGMENT_SIMPLE_NAME, "true");
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

    private static final class TenantNameAndEndpoint {
        
        private final String tenantName;
        private final String authenticationEndpoint;

        TenantNameAndEndpoint(String tenantName, String authenticationEndpoint) {
            Preconditions.checkArgumentNotNull(tenantName, "tenantName");
            Preconditions.checkArgumentNotNull(authenticationEndpoint, "authenticationEndpoint");
            this.tenantName = tenantName;
            this.authenticationEndpoint = authenticationEndpoint;
        }
        
        String tenantName() {
            return tenantName;
        }
        
        String authenticationEndpoint() {
            return authenticationEndpoint;
        }
    }
}
