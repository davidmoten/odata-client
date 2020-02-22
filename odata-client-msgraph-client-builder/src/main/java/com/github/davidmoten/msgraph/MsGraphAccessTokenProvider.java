package com.github.davidmoten.msgraph;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.davidmoten.guavamini.Preconditions;
import com.github.davidmoten.odata.client.internal.Util;

public final class MsGraphAccessTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(MsGraphAccessTokenProvider.class);

    private static final int OK = 200;
    private static final String POST = "POST";
    private static final String APPLICATION_JSON = "application/json";
    private static final String REQUEST_HEADER = "Accept";
    private static final String SCOPE_MS_GRAPH_DEFAULT = "https://graph.microsoft.com/.default";
    private static final String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";
    private static final String RESOURCE_MS_GRAPH = "https://graph.microsoft.com/";
    private static final String OAUTH2_TOKEN_URL_PREFIX = "https://login.windows.net/";
    private static final String OAUTH2_TOKEN_URL_SUFFIX = "/oauth2/token";

    private static final String PARAMETER_SCOPE = "scope";
    private static final String PARAMETER_CLIENT_SECRET = "client_secret";
    private static final String PARAMETER_GRANT_TYPE = "grant_type";
    private static final String PARAMETER_CLIENT_ID = "client_id";
    private static final String PARAMETER_RESOURCE = "resource";

    private final String tenantName;
    private final String clientId;
    private final String clientSecret;
    private final long refreshBeforeExpiryMs;
    private final long connectTimeoutMs;
    private final long readTimeoutMs;

    private long expiryTime;
    private String accessToken;

    private MsGraphAccessTokenProvider(String tenantName, String clientId, String clientSecret,
            long refreshBeforeExpiryMs, long connectTimeoutMs, long readTimeoutMs) {
        Preconditions.checkNotNull(tenantName);
        Preconditions.checkNotNull(clientId);
        Preconditions.checkNotNull(clientSecret);
        Preconditions.checkArgument(refreshBeforeExpiryMs >= 0,
                "refreshBeforeExpiryMs must be >=0");
        Preconditions.checkArgument(connectTimeoutMs >= 0, "connectTimeoutMs must be >=0");
        Preconditions.checkArgument(readTimeoutMs >= 0, "readTimeoutMs must be >=0");
        this.tenantName = tenantName;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.refreshBeforeExpiryMs = refreshBeforeExpiryMs;
        this.connectTimeoutMs = connectTimeoutMs;
        this.readTimeoutMs = readTimeoutMs;

    }

    public static Builder tenantName(String tenantName) {
        return new Builder(tenantName);
    }

    public synchronized String get() {
        long now = System.currentTimeMillis();
        if (accessToken != null && now < expiryTime - refreshBeforeExpiryMs) {
            return accessToken;
        } else {
            return refreshAccessToken();
        }
    }

    private String refreshAccessToken() {
        try {
            log.debug("refreshing access token");
            URL url = new URL(OAUTH2_TOKEN_URL_PREFIX + tenantName + OAUTH2_TOKEN_URL_SUFFIX);
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            con.setConnectTimeout((int) connectTimeoutMs);
            con.setReadTimeout((int) readTimeoutMs);
            con.setRequestMethod(POST);
            con.setRequestProperty(REQUEST_HEADER, APPLICATION_JSON);
            StringBuilder params = new StringBuilder();
            add(params, PARAMETER_RESOURCE, RESOURCE_MS_GRAPH);
            add(params, PARAMETER_CLIENT_ID, clientId);
            add(params, PARAMETER_GRANT_TYPE, GRANT_TYPE_CLIENT_CREDENTIALS);
            add(params, PARAMETER_CLIENT_SECRET, clientSecret);
            add(params, PARAMETER_SCOPE, SCOPE_MS_GRAPH_DEFAULT);
            con.setDoOutput(true);
            try (DataOutputStream dos = new DataOutputStream(con.getOutputStream())) {
                dos.writeBytes(params.toString());
            }
            int responseCode = con.getResponseCode();

            String json = Util.readString(con.getInputStream(), StandardCharsets.UTF_8);

            if (responseCode != OK) {
                throw new IOException("Response code=" + responseCode + ", output=" + json);
            } else {
                ObjectMapper om = new ObjectMapper();
                JsonNode o = om.readTree(json);
                // update the cached values
                expiryTime = o.get("expires_on").asLong() * 1000;
                accessToken = o.get("access_token").asText();
                log.debug("refreshed access token");
                return accessToken;
            }
        } catch (IOException e) {
            // reset stuff
            expiryTime = 0;
            accessToken = null;
            throw new RuntimeException(e);
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

    public static final class Builder {
        final String tenantName;
        String clientId;
        String clientSecret;

        // default to refresh access token on every call of get()
        long refreshBeforeExpiryMs = Long.MAX_VALUE;
        long connectTimeoutMs = TimeUnit.SECONDS.toMillis(30);
        long readTimeoutMs = TimeUnit.SECONDS.toMillis(30);

        Builder(String tenantName) {
            this.tenantName = tenantName;
        }

        public Builder2 clientId(String clientId) {
            this.clientId = clientId;
            return new Builder2(this);
        }

    }

    public static final class Builder2 {
        private final Builder b;

        Builder2(Builder b) {
            this.b = b;
        }

        public Builder3 clientSecret(String clientSecret) {
            b.clientSecret = clientSecret;
            return new Builder3(b);
        }

    }

    public static final class Builder3 {

        private final Builder b;

        Builder3(Builder b) {
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
        public Builder3 refreshBeforeExpiry(long duration, TimeUnit unit) {
            b.refreshBeforeExpiryMs = unit.toMillis(duration);
            return this;
        }
        
        public Builder3 connectTimeoutMs(long duration, TimeUnit unit) {
            b.connectTimeoutMs = unit.toMillis(duration);
            return this;
        }
        
        public Builder3 readTimeoutMs(long duration, TimeUnit unit) {
            b.readTimeoutMs = unit.toMillis(duration);
            return this;
        }

        public MsGraphAccessTokenProvider build() {
            return new MsGraphAccessTokenProvider(b.tenantName, b.clientId, b.clientSecret,
                    b.refreshBeforeExpiryMs, b.connectTimeoutMs, b.readTimeoutMs);
        }
    }

}
