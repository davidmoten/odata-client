package com.github.davidmoten.odata.msgraph;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.impl.client.HttpClientBuilder;

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

    private static final String AUTHORIZATION_HEADER_NAME = "Authorization";
    private static final String OAUTH_BEARER_PREFIX = "Bearer ";

    private MsGraph() {
        // prevent instantiation
    }

    public Builder tenantName(String tenantName) {
        return new Builder(tenantName);
    }

    public static final class Builder {
        String tenantName;
        String clientId;
        String clientSecret;
        long duration = 5;
        TimeUnit unit = TimeUnit.MINUTES;

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
            b.duration = duration;
            b.unit = unit;
            return this;
        }

        public GraphService build() {
            return createService(b.tenantName, b.clientId, b.clientSecret, b.duration, b.unit);
        }
    }

    private static GraphService createService(String tenantName, String clientId, String clientSecret,
            long refreshBeforeExpiryDuration, TimeUnit refreshBeforeExpiryUnit) {
        MsGraphAccessTokenProvider accessTokenProvider = MsGraphAccessTokenProvider //
                .tenantName(tenantName) //
                .clientId(clientId) //
                .clientSecret(clientSecret) //
                .refreshBeforeExpiry(refreshBeforeExpiryDuration, refreshBeforeExpiryUnit) //
                .build();
        Path basePath = new Path("https://graph.microsoft.com/v1.0", PathStyle.IDENTIFIERS_AS_SEGMENTS);
        HttpService httpService = new ApacheHttpClientHttpService( //
                basePath, //
                () -> HttpClientBuilder.create().build(), //
                m -> authenticate(m, accessTokenProvider));
        return new GraphService(new Context(Serializer.INSTANCE, httpService));
    }

    private static List<RequestHeader> authenticate(List<RequestHeader> m,
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
