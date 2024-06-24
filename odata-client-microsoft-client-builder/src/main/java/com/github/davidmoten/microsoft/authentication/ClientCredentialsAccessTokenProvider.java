package com.github.davidmoten.microsoft.authentication;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.guavamini.annotations.VisibleForTesting;
import com.github.davidmoten.odata.client.ClientException;
import com.github.davidmoten.odata.client.internal.Util;

public final class ClientCredentialsAccessTokenProvider implements AccessTokenProvider {

    private static final Logger log = LoggerFactory
            .getLogger(ClientCredentialsAccessTokenProvider.class);

    private static final int OK = 200;
    private static final String POST = "POST";
    private static final String APPLICATION_JSON = "application/json";
    private static final String REQUEST_HEADER = "Accept";
    private static final String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";

    private static final String PARAMETER_SCOPE = "scope";
    private static final String PARAMETER_CLIENT_SECRET = "client_secret";
    private static final String PARAMETER_GRANT_TYPE = "grant_type";
    private static final String PARAMETER_CLIENT_ID = "client_id";
    private static final String PARAMETER_RESOURCE = "resource";

    private final String url;
    private final String clientId;
    private final String clientSecret;
    private final long refreshBeforeExpiryMs;
    private final long connectTimeoutMs;
    private final long readTimeoutMs;
    private final List<String> scopes;
    private final String resource;
    private final Optional<String> proxyHost;
    private final Optional<Integer> proxyPort;
    private final Optional<String> proxyUsername;
    private final Optional<String> proxyPassword;

    private long expiryTime;
    private String accessToken;

    private ClientCredentialsAccessTokenProvider(String url, String clientId,
            String clientSecret, long refreshBeforeExpiryMs, long connectTimeoutMs,
            long readTimeoutMs, String resource, List<String> scopes,
            Optional<String> proxyHost, //
            Optional<Integer> proxyPort, //
            Optional<String> proxyUsername, Optional<String> proxyPassword) {
        Preconditions.checkNotNull(url);
        Preconditions.checkNotNull(clientId);
        Preconditions.checkNotNull(clientSecret);
        Preconditions.checkArgument(refreshBeforeExpiryMs >= 0,
                "refreshBeforeExpiryMs must be >=0");
        Preconditions.checkArgument(connectTimeoutMs >= 0, "connectTimeoutMs must be >=0");
        Preconditions.checkArgument(readTimeoutMs >= 0, "readTimeoutMs must be >=0");
        Preconditions.checkNotNull(proxyHost);
        Preconditions.checkNotNull(proxyPort);
        Preconditions.checkNotNull(proxyUsername);
        Preconditions.checkNotNull(proxyPassword);
        Preconditions.checkArgument(!proxyHost.isPresent() || proxyPort.isPresent(),
                "if proxyHost specified then so must proxyPort be specified");
        Preconditions.checkNotNull(resource);
        Preconditions.checkNotNull(scopes);
        this.url = url;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.refreshBeforeExpiryMs = refreshBeforeExpiryMs;
        this.connectTimeoutMs = connectTimeoutMs;
        this.readTimeoutMs = readTimeoutMs;
        this.scopes = scopes;
        this.resource = resource;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.proxyUsername = proxyUsername;
        this.proxyPassword = proxyPassword;
    }
    
    /**
     * Returns the token provider chained builder.
     * 
     * @param url the url that supplies tokens
     * @return builder
     */
    public static BuilderWithUrl url(String url) {
        return new BuilderWithUrl(url);
    }

    @Override
    public synchronized String get() {
        long now = System.currentTimeMillis();
        if (accessToken != null && now < expiryTime - refreshBeforeExpiryMs) {
            return accessToken;
        } else {
            return refreshAccessToken();
        }
    }

    private String refreshAccessToken() {

        // post some parameters in json format to the access token url
        // and record returned expiry information so they we know when we
        // need to refresh the token
        try {
            log.debug("refreshing access token");
            URL url = new URL(this.url);
            final HttpsURLConnection con;
            if (proxyHost.isPresent()) {
                InetSocketAddress proxyInet = new InetSocketAddress(proxyHost.get(),
                        proxyPort.get());
                Proxy proxy = new Proxy(Proxy.Type.HTTP, proxyInet);
                con = (HttpsURLConnection) url.openConnection(proxy);
                if (proxyUsername.isPresent()) {
                    String usernameAndPassword = proxyUsername.get() + ":" + proxyPassword.get();
                    String authString = "Basic " + Base64.getEncoder()
                            .encodeToString(usernameAndPassword.getBytes(StandardCharsets.UTF_8));
                    con.setRequestProperty("Proxy-Authorization", authString);
                }
                // TODO support NTLM?
            } else {
                con = (HttpsURLConnection) url.openConnection();
            }
            con.setConnectTimeout((int) connectTimeoutMs);
            con.setReadTimeout((int) readTimeoutMs);
            con.setRequestMethod(POST);
            con.setRequestProperty(REQUEST_HEADER, APPLICATION_JSON);
            StringBuilder params = new StringBuilder();
            add(params, PARAMETER_RESOURCE, resource);
            add(params, PARAMETER_CLIENT_ID, clientId);
            add(params, PARAMETER_GRANT_TYPE, GRANT_TYPE_CLIENT_CREDENTIALS);
            add(params, PARAMETER_CLIENT_SECRET, clientSecret);
            for (String scope : scopes) {
                add(params, PARAMETER_SCOPE, scope);
            }
            con.setDoOutput(true);
            try (DataOutputStream dos = new DataOutputStream(con.getOutputStream())) {
                dos.writeBytes(params.toString());
            }
            int responseCode = con.getResponseCode();

            String json = Util.readString(con.getInputStream(), StandardCharsets.UTF_8);

            if (responseCode != OK) {
                throw new ClientException(responseCode, json);
            } else {
                ObjectMapper om = new ObjectMapper();
                JsonNode o = om.readTree(json);
                // update the cached values
                expiryTime = o.get("expires_on").asLong() * 1000;
                accessToken = o.get("access_token").asText();
                log.debug("refreshed access token, expires on " + new Date(expiryTime));
                return accessToken;
            }
        } catch (IOException e) {
            // reset stuff
            expiryTime = 0;
            accessToken = null;
            
            Optional<Integer> code = extractStatusCode(e.getMessage());
            if (code.isPresent()) {
                throw new ClientException(code.get(), e);
            } else {
                throw new ClientException(e);
            }
        }
    }
    
    private static final Pattern RESPONSE_CODE_PATTERN = Pattern.compile("^Server returned HTTP response code: (\\d+) for.*");
    
    @VisibleForTesting
    static Optional<Integer> extractStatusCode(String message) {
        if (message == null) {
            return Optional.empty();
        }
        Matcher m = RESPONSE_CODE_PATTERN.matcher(message);
        if (m.find()) {
            String code = m.group(1);
            return Optional.of(Integer.parseInt(code));
        } else {
            return Optional.empty();
        }
    }

    private static void add(StringBuilder params, String key, String value) {
        if (params.length() > 0) {
            params.append("&");
        }
        params.append(key);
        params.append("=");
        try {
            params.append(URLEncoder.encode(value, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static final class BuilderWithUrl {
        final String url;
        List<String> scopes = new ArrayList<>();
        String resource;
        String clientId;
        String clientSecret;

        // default to refresh access token on every call of get()
        long refreshBeforeExpiryMs = Long.MAX_VALUE;
        long connectTimeoutMs = TimeUnit.SECONDS.toMillis(30);
        long readTimeoutMs = TimeUnit.SECONDS.toMillis(30);
        Optional<String> proxyHost = Optional.empty();
        Optional<Integer> proxyPort = Optional.empty();
        Optional<String> proxyUsername = Optional.empty();
        Optional<String> proxyPassword = Optional.empty();

        BuilderWithUrl(String url) {
            this.url = url;
        }

        public BuilderWithResource resource(String resource) {
            this.resource = resource;
            return new BuilderWithResource(this);
        }

    }

    public static final class BuilderWithResource {

        private final BuilderWithUrl b;

        BuilderWithResource(BuilderWithUrl b) {
            this.b = b;
        }

        public BuilderWithScope scope(List<String> scopes) {
            Preconditions.checkNotNull(scopes);
            b.scopes.addAll(scopes);
            return new BuilderWithScope(b);
        }

    }

    public static final class BuilderWithScope {

        private final BuilderWithUrl b;

        public BuilderWithScope(BuilderWithUrl b) {
            this.b = b;
        }

        public BuilderWithClientId clientId(String clientId) {
            b.clientId = clientId;
            return new BuilderWithClientId(b);
        }
    }

    public static final class BuilderWithClientId {
        private final BuilderWithUrl b;

        BuilderWithClientId(BuilderWithUrl b) {
            this.b = b;
        }

        public BuilderWithClientSecret clientSecret(String clientSecret) {
            b.clientSecret = clientSecret;
            return new BuilderWithClientSecret(b);
        }

    }

    public static final class BuilderWithClientSecret {

        private final BuilderWithUrl b;

        BuilderWithClientSecret(BuilderWithUrl b) {
            this.b = b;
        }

        /**
         * The access token is returned from AD with an expiry time. If you call
         * {@code get()} within {@code duration} of the expiry time then a refresh of
         * the access token will be performed. If this value is not set then the access
         * token is refreshed on every call of {@code get()}.
         * 
         * @param duration duration before expiry time after which point a refresh will
         *                 be run (on next authentication attempt)
         * @param unit     time unit for the duration
         * @return builder
         */
        public BuilderWithClientSecret refreshBeforeExpiry(long duration, TimeUnit unit) {
            b.refreshBeforeExpiryMs = unit.toMillis(duration);
            return this;
        }

        public BuilderWithClientSecret connectTimeoutMs(long duration, TimeUnit unit) {
            b.connectTimeoutMs = unit.toMillis(duration);
            return this;
        }

        public BuilderWithClientSecret readTimeoutMs(long duration, TimeUnit unit) {
            b.readTimeoutMs = unit.toMillis(duration);
            return this;
        }

        public BuilderWithClientSecret scope(List<String> scopes) {
            b.scopes.addAll(scopes);
            return this;
        }

        public BuilderWithClientSecret proxyHost(String proxyHost) {
            Preconditions.checkNotNull(proxyHost);
            return proxyHost(Optional.of(proxyHost));
        }

        public BuilderWithClientSecret proxyPort(int proxyPort) {
            return proxyPort(Optional.of(proxyPort));
        }

        public BuilderWithClientSecret proxyUsername(String username) {
            Preconditions.checkNotNull(username);
            return proxyUsername (Optional.of(username));
        }

        public BuilderWithClientSecret proxyPassword(String password) {
            Preconditions.checkNotNull(password);
            return proxyPassword(Optional.of(password));
        }
        
        public BuilderWithClientSecret proxyHost(Optional<String> proxyHost) {
            Preconditions.checkNotNull(proxyHost);
            b.proxyHost = proxyHost;
            return this;
        }

        public BuilderWithClientSecret proxyPort(Optional<Integer> proxyPort) {
            Preconditions.checkNotNull(proxyPort);
            b.proxyPort = proxyPort;
            return this;
        }

        public BuilderWithClientSecret proxyUsername(Optional<String> username) {
            Preconditions.checkNotNull(username);
            b.proxyUsername = username;
            return this;
        }

        public BuilderWithClientSecret proxyPassword(Optional<String> password) {
            Preconditions.checkNotNull(password);
            b.proxyPassword = password;
            return this;
        }

        public ClientCredentialsAccessTokenProvider build() {
            return new ClientCredentialsAccessTokenProvider(b.url, b.clientId,
                    b.clientSecret, b.refreshBeforeExpiryMs, b.connectTimeoutMs, b.readTimeoutMs,
                    b.resource, b.scopes, b.proxyHost, b.proxyPort, b.proxyUsername,
                    b.proxyPassword);
        }
    }

}
